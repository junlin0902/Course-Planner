package com.example.data

import com.example.plugins.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.time.LocalDateTime


private const val DATABASE_NAME = "UserDatabase.db"

private const val TABLE_USERS = "users"
private const val COLUMN_ID = "id"
private const val COLUMN_USERNAME = "username"
private const val COLUMN_PASSWORD = "password"

private const val TABLE_COURSE_REVIEWS = "course_reviews"
private const val COLUMN_REVIEW_ID = "review_id"
private const val COLUMN_USER_ID = "user_id"
private const val COLUMN_COURSE_CODE = "course_code"
private const val COLUMN_REVIEW_DATE = "date"
private const val COLUMN_CONTENT = "content"
private const val COLUMN_RATING = "rating"

private const val TABLE_ENROLLMENTS = "enrollments"
private const val COLUMN_ENROLLMENT_USER = "username"
private const val COLUMN_ENROLLMENT_DAY = "enrollment_day"
private const val COLUMN_ENROLLMENT_START = "enrollment_start"
private const val COLUMN_ENROLLMENT_END = "enrollment_end"
private const val COLUMN_ENROLLMENT_CODE = "enrollment_code"
private const val COLUMN_ENROLLMENT_DESC = "enrollment_description"

private const val TABLE_FILES = "files"
private const val COLUMN_REF_COUNT = "ref_count"
private const val COLUMN_FILE_ID = "file_id"
private const val COLUMN_FILE_NAME = "file_name"
private const val COLUMN_FILE_URI = "file_uri"

private const val TABLE_FOLDERS = "folders"
private const val COLUMN_FOLDER_ID = "folder_id"
private const val COLUMN_FOLDER_NAME = "folder_name"

@Serializable
data class FileRecord(var id: Int, var name: String, var uri: String)
@Serializable
data class FolderRecord(var id: Int, var name: String)
@Serializable
data class Event(
    val name: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    var start: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    var end: LocalDateTime,
    val description: String? = null,
)
@Serializable
data class CourseReview(val reviewer: String, val courseCode: String, val date: String, val content: String, val stars: Int)
@Serializable
data class Course(val code: String, val title: String, val description: String)
@Serializable
data class CourseSchedule(val section: String, val enrollment: Int, val maxEnrollment: Int, val meetStart: String, val meetEnd: String, val meetDays: String)


object DatabaseConnection {
    init {
        // Load the JDBC driver
        Class.forName("org.sqlite.JDBC")
        createTables()
    }

    fun getConnection(): Connection = DriverManager.getConnection("jdbc:sqlite:$DATABASE_NAME")

    private fun createTables() {
        val connection: Connection? = null
        try {
            val statement = getConnection().createStatement()

//            Drop existing tables
//            val dropUsersTableSQL = "DROP TABLE IF EXISTS $TABLE_USERS;"
//            val dropCourseReviewsTableSQL = "DROP TABLE IF EXISTS $TABLE_COURSE_REVIEWS;"
//            val dropEnrollmentTableSQL = "DROP TABLE IF EXISTS $TABLE_ENROLLMENTS;"
//            val dropFilesTableSQL = "DROP TABLE IF EXISTS $TABLE_FILES;"
//            val dropFoldersTableSQL = "DROP TABLE IF EXISTS $TABLE_FOLDERS"
//
//            statement.execute(dropUsersTableSQL)
//            statement.execute(dropCourseReviewsTableSQL)
//            statement.execute(dropEnrollmentTableSQL)
//            statement.execute(dropFilesTableSQL)
//            statement.execute(dropFoldersTableSQL)

            val createUsersTableSQL = """
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USERNAME TEXT NOT NULL,
                    $COLUMN_PASSWORD TEXT NOT NULL
                );
            """.trimIndent()

            val createReviewsTableStatement = """
                CREATE TABLE IF NOT EXISTS $TABLE_COURSE_REVIEWS (
                    $COLUMN_REVIEW_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_ID INTEGER,
                    $COLUMN_COURSE_CODE TEXT,
                    $COLUMN_REVIEW_DATE TEXT,
                    $COLUMN_CONTENT TEXT,
                    $COLUMN_RATING INTEGER,
                    FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
                );
            """.trimIndent()

            val createEnrollmentTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_ENROLLMENTS (
                    $COLUMN_ENROLLMENT_USER TEXT,
                    $COLUMN_ENROLLMENT_CODE TEXT,
                    $COLUMN_ENROLLMENT_DESC TEXT,
                    $COLUMN_ENROLLMENT_DAY TEXT,
                    $COLUMN_ENROLLMENT_START TEXT,
                    $COLUMN_ENROLLMENT_END INTEGER,
                    FOREIGN KEY($COLUMN_ENROLLMENT_USER) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
                );
            """.trimIndent()

            val createFilesTableStatement = """
                CREATE TABLE IF NOT EXISTS $TABLE_FILES (
                    $COLUMN_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_FILE_NAME TEXT,
                    $COLUMN_FILE_URI TEXT,
                    $COLUMN_FOLDER_ID INTEGER,
                    $COLUMN_REF_COUNT INTEGER DEFAULT 1,
                    FOREIGN KEY($COLUMN_FOLDER_ID) REFERENCES $TABLE_FOLDERS($COLUMN_FOLDER_ID)
                );
            """.trimIndent()

            val createFoldersTableStatement = """
                CREATE TABLE IF NOT EXISTS $TABLE_FOLDERS (
                    $COLUMN_FOLDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_FOLDER_NAME TEXT
                );
            """.trimIndent()

            statement.execute(createUsersTableSQL)
            statement.execute(createReviewsTableStatement)
            statement.execute(createEnrollmentTable)
            statement.execute(createFilesTableStatement)
            statement.execute(createFoldersTableStatement)

        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
}

class UserDB {
    private fun checkUser(username: String): Boolean {
        val sql = "SELECT COUNT($COLUMN_ID) FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, username)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0
            }
            return false
        }
    }
    fun addUser(username: String, password: String): Int {
        if (checkUser(username)) {
            return 2
        }
        val hashedPassword = hashPassword(password)
        val sql = "INSERT INTO $TABLE_USERS ($COLUMN_USERNAME, $COLUMN_PASSWORD) VALUES (?, ?)"
        DatabaseConnection.getConnection().use { connection ->
            val statement: PreparedStatement = connection.prepareStatement(sql).apply {
                setString(1, username)
                setString(2, hashedPassword)
            }
            return statement.executeUpdate()
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun validateUser(username: String, password: String): Boolean {
        val hashedPassword = hashPassword(password)
        val sql = "SELECT COUNT($COLUMN_ID) FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, username)
                setString(2, hashedPassword)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0
            }
            return false
        }
    }

    fun changePassword(username: String, oldPassword: String, newPassword: String): Int {
        if (!validateUser(username, oldPassword)) {
            return 2
        }

        val hashedNewPassword = hashPassword(newPassword)

        val sql = "UPDATE $TABLE_USERS SET $COLUMN_PASSWORD = ? WHERE $COLUMN_USERNAME = ?"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, hashedNewPassword)
                setString(2, username)
            }
            return statement.executeUpdate()
        }
    }

}

class ReviewDB {
    fun addReview(courseReview: CourseReview): Boolean {
        val userId = getUserId(courseReview.reviewer)
        if (userId == -1) {
            return false
        }
        val sql = """
            INSERT INTO $TABLE_COURSE_REVIEWS ($COLUMN_USER_ID, $COLUMN_COURSE_CODE, $COLUMN_REVIEW_DATE, $COLUMN_CONTENT, $COLUMN_RATING) 
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setInt(1, userId)
                setString(2, courseReview.courseCode)
                setString(3, courseReview.date)
                setString(4, courseReview.content)
                setInt(5, courseReview.stars)
            }
            val rowsInserted = statement.executeUpdate()
            return rowsInserted > 0
        }
    }

    private fun getUserId(username: String): Int {
        val sql = "SELECT $COLUMN_ID FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, username)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt(1)
            }
            return -1
        }
    }

    fun getAllReviewsFrom(courseCode: String): List<CourseReview> {
        val sql = """
            SELECT u.$COLUMN_USERNAME, r.$COLUMN_REVIEW_DATE, r.$COLUMN_CONTENT, r.$COLUMN_RATING
            FROM $TABLE_COURSE_REVIEWS r
            INNER JOIN $TABLE_USERS u ON r.$COLUMN_USER_ID = u.$COLUMN_ID
            WHERE r.$COLUMN_COURSE_CODE = ?
            ORDER BY r.$COLUMN_RATING DESC
        """.trimIndent()

        val reviews: MutableList<CourseReview> = mutableListOf()

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, courseCode)
            }
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val username = resultSet.getString(1)
                val reviewDate = resultSet.getString(2)
                val content = resultSet.getString(3)
                val rating = resultSet.getInt(4)

                val review = CourseReview(username, courseCode, reviewDate, content, rating)
                reviews.add(review)
            }
        }

        return reviews
    }
}

class EnrollmentDB {
    fun getAllEnrollments(currentUser: String): MutableList<Event> {
        val ret: MutableList<Event> = mutableListOf()
        val sql = "SELECT $COLUMN_ENROLLMENT_CODE, $COLUMN_ENROLLMENT_DAY, $COLUMN_ENROLLMENT_START, $COLUMN_ENROLLMENT_END, $COLUMN_ENROLLMENT_DESC FROM $TABLE_ENROLLMENTS WHERE $COLUMN_ENROLLMENT_USER = ?"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, currentUser)
            }
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val code = resultSet.getString(COLUMN_ENROLLMENT_CODE)
                val day = resultSet.getString(COLUMN_ENROLLMENT_DAY)
                var start = LocalDateTime.parse(resultSet.getString(COLUMN_ENROLLMENT_START))
                var end = LocalDateTime.parse(resultSet.getString(COLUMN_ENROLLMENT_END))
                val desc = resultSet.getString(COLUMN_ENROLLMENT_DESC)

                for (char in day) {
                    when (char) {
                        'M' -> {
                            start = start.withYear(2023)
                            start = start.withMonth(5)
                            start = start.withDayOfMonth(15)
                            end = end.withYear(2023)
                            end = end.withMonth(5)
                            end = end.withDayOfMonth(15)
                        }
                        'T' -> {
                            start = start.withYear(2023)
                            start = start.withMonth(5)
                            start = start.withDayOfMonth(16)
                            end = end.withYear(2023)
                            end = end.withMonth(5)
                            end = end.withDayOfMonth(16)
                        }
                        'W' -> {
                            start = start.withYear(2023)
                            start = start.withMonth(5)
                            start = start.withDayOfMonth(17)
                            end = end.withYear(2023)
                            end = end.withMonth(5)
                            end = end.withDayOfMonth(17)
                        }
                        'R' -> {
                            start = start.withYear(2023)
                            start = start.withMonth(5)
                            start = start.withDayOfMonth(18)
                            end = end.withYear(2023)
                            end = end.withMonth(5)
                            end = end.withDayOfMonth(18)
                        }
                        'F' -> {
                            start = start.withYear(2023)
                            start = start.withMonth(5)
                            start = start.withDayOfMonth(19)
                            end = end.withYear(2023)
                            end = end.withMonth(5)
                            end = end.withDayOfMonth(19)
                        }
                    }
                    ret.add(Event(code, start, end, desc))
                }
            }
        }
        return ret
    }

    fun addEnrollment(currentUser: String, cs: CourseSchedule, course: Course): Boolean {
        val sql = "INSERT INTO $TABLE_ENROLLMENTS ($COLUMN_ENROLLMENT_USER, $COLUMN_ENROLLMENT_CODE, $COLUMN_ENROLLMENT_DESC, $COLUMN_ENROLLMENT_DAY, $COLUMN_ENROLLMENT_START, $COLUMN_ENROLLMENT_END) VALUES (?, ?, ?, ?, ?, ?)"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, currentUser)
                setString(2, course.code)
                setString(3, course.description)
                setString(4, cs.meetDays)
                setString(5, cs.meetStart)
                setString(6, cs.meetEnd)
            }
            val rowsInserted = statement.executeUpdate()
            return rowsInserted > 0
        }
    }
}

class FileFolderDB {
    fun addFileToFolder(folderId: Int, fileName: String, fileUri: String): Boolean {
        DatabaseConnection.getConnection().use { connection ->
            connection.autoCommit = false

            // Check for existing ref_count
            var count = 1
            val selectQuery = "SELECT $COLUMN_REF_COUNT FROM $TABLE_FILES WHERE $COLUMN_FILE_URI = ?"
            val selectStmt = connection.prepareStatement(selectQuery)
            selectStmt.setString(1, fileUri)
            val resultSet = selectStmt.executeQuery()
            if (resultSet.next()) {
                count = resultSet.getInt(1)
            }
            resultSet.close()
            selectStmt.close()

            // Insert new file record
            val insertQuery = "INSERT INTO $TABLE_FILES ($COLUMN_FILE_NAME, $COLUMN_FILE_URI, $COLUMN_FOLDER_ID, $COLUMN_REF_COUNT) VALUES (?, ?, ?, ?)"
            val insertStmt = connection.prepareStatement(insertQuery)
            insertStmt.setString(1, fileName)
            insertStmt.setString(2, fileUri)
            insertStmt.setInt(3, folderId)
            insertStmt.setInt(4, count)
            val insertSuccessful = insertStmt.executeUpdate() > 0
            insertStmt.close()

            if (insertSuccessful) {
                // Update ref_count for all records with the same fileUri
                val updateQuery = "UPDATE $TABLE_FILES SET $COLUMN_REF_COUNT = $COLUMN_REF_COUNT + 1 WHERE $COLUMN_FILE_URI = ?"
                val updateStmt = connection.prepareStatement(updateQuery)
                updateStmt.setString(1, fileUri)
                updateStmt.executeUpdate()
                updateStmt.close()
            }

            connection.commit()
            return insertSuccessful
        }
    }

    fun deleteFile(fileId: Int): Boolean {
        DatabaseConnection.getConnection().use { connection ->
            connection.autoCommit = false

            // Fetch file URI
            var filePath = ""
            val selectQuery = "SELECT $COLUMN_FILE_URI FROM $TABLE_FILES WHERE $COLUMN_FILE_ID = ?"
            val selectStmt = connection.prepareStatement(selectQuery)
            selectStmt.setInt(1, fileId)
            val resultSet = selectStmt.executeQuery()
            if (resultSet.next()) {
                filePath = resultSet.getString(1)
            }
            resultSet.close()
            selectStmt.close()

            // Delete file record
            val deleteQuery = "DELETE FROM $TABLE_FILES WHERE $COLUMN_FILE_ID = ?"
            val deleteStmt = connection.prepareStatement(deleteQuery)
            deleteStmt.setInt(1, fileId)
            val deleteResult = deleteStmt.executeUpdate() > 0
            deleteStmt.close()

            // Handle additional logic based on filePath and deleteResult

            connection.commit()
            return deleteResult
        }
    }

    fun getFilesInFolder(folderId: Int): List<FileRecord> {
        val fileList = mutableListOf<FileRecord>()
        DatabaseConnection.getConnection().use { connection ->
            val query = "SELECT $COLUMN_FILE_ID, $COLUMN_FILE_NAME, $COLUMN_FILE_URI FROM $TABLE_FILES WHERE $COLUMN_FOLDER_ID = ?"
            val stmt = connection.prepareStatement(query)
            stmt.setInt(1, folderId)
            val resultSet = stmt.executeQuery()
            while (resultSet.next()) {
                val fileId = resultSet.getInt(1)
                val fileName = resultSet.getString(2)
                val fileUri = resultSet.getString(3)
                fileList.add(FileRecord(fileId, fileName, fileUri))
            }
            resultSet.close()
            stmt.close()
        }
        return fileList
    }

    fun renameFile(fileId: Int, newName: String): Boolean {
        DatabaseConnection.getConnection().use { connection ->
            val query = "UPDATE $TABLE_FILES SET $COLUMN_FILE_NAME = ? WHERE $COLUMN_FILE_ID = ?"
            val stmt = connection.prepareStatement(query)
            stmt.setString(1, newName)
            stmt.setInt(2, fileId)
            val result = stmt.executeUpdate() > 0
            stmt.close()
            return result
        }
    }

    fun isFileExistInFolder(folderId: Int, fileName: String): Boolean {
        DatabaseConnection.getConnection().use { connection ->
            val query = "SELECT COUNT(*) FROM $TABLE_FILES WHERE $COLUMN_FILE_NAME = ? AND $COLUMN_FOLDER_ID = ?"
            val stmt = connection.prepareStatement(query)
            stmt.setString(1, fileName)
            stmt.setInt(2, folderId)
            val resultSet = stmt.executeQuery()
            val exists = resultSet.next() && resultSet.getInt(1) > 0
            resultSet.close()
            stmt.close()
            return exists
        }
    }

    fun addFolder(folderName: String): Boolean {
        DatabaseConnection.getConnection().use { connection ->
            val query = "INSERT INTO $TABLE_FOLDERS ($COLUMN_FOLDER_NAME) VALUES (?)"
            val stmt = connection.prepareStatement(query)
            stmt.setString(1, folderName)
            val result = stmt.executeUpdate() > 0
            stmt.close()
            return result
        }
    }

    fun getAllFolders(): List<FolderRecord> {
        val folders = mutableListOf<FolderRecord>()
        DatabaseConnection.getConnection().use { connection ->
            val query = "SELECT $COLUMN_FOLDER_ID, $COLUMN_FOLDER_NAME FROM $TABLE_FOLDERS"
            val stmt = connection.prepareStatement(query)
            val resultSet = stmt.executeQuery()
            while (resultSet.next()) {
                val folderId = resultSet.getInt(1)
                val folderName = resultSet.getString(2)
                folders.add(FolderRecord(folderId, folderName))
            }
            resultSet.close()
            stmt.close()
        }
        return folders
    }

    fun deleteFolder(folderId: Int): Boolean {
        DatabaseConnection.getConnection().use { connection ->
            val query = "DELETE FROM $TABLE_FOLDERS WHERE $COLUMN_FOLDER_ID = ?"
            val stmt = connection.prepareStatement(query)
            stmt.setInt(1, folderId)
            val result = stmt.executeUpdate() > 0
            stmt.close()
            return result
        }
    }

    fun renameFolder(folderId: Int, newName: String): Boolean {
        DatabaseConnection.getConnection().use { connection ->
            val query = "UPDATE $TABLE_FOLDERS SET $COLUMN_FOLDER_NAME = ? WHERE $COLUMN_FOLDER_ID = ?"
            val stmt = connection.prepareStatement(query)
            stmt.setString(1, newName)
            stmt.setInt(2, folderId)
            val result = stmt.executeUpdate() > 0
            stmt.close()
            return result
        }
    }

    fun folderNameExists(name: String): Boolean {
        DatabaseConnection.getConnection().use { connection ->
            val query = "SELECT COUNT(*) FROM $TABLE_FOLDERS WHERE $COLUMN_FOLDER_NAME = ?"
            val stmt = connection.prepareStatement(query)
            stmt.setString(1, name)
            val resultSet = stmt.executeQuery()
            val exists = resultSet.next() && resultSet.getInt(1) > 0
            resultSet.close()
            stmt.close()
            return exists
        }
    }
}