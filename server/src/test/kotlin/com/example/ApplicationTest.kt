package com.example

import com.example.data.FolderRecord
import com.example.plugins.LocalDateTimeSerializer
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import kotlin.test.assertEquals

@Serializable
data class UserCredentials(val username: String, val password: String)
@Serializable
data class CourseReview(val reviewer: String, val courseCode: String, val date: String, val content: String, val stars: Int)
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
data class Course(val code: String, val title: String, val description: String)
@Serializable
data class CourseSchedule(val section: String, val enrollment: Int, val maxEnrollment: Int, val meetStart: String, val meetEnd: String, val meetDays: String)
@Serializable
data class EnrollmentRequest(val currentUser: String, val cs: CourseSchedule, val course: Course)
@Serializable
data class FileToFolderRequest(val folderId: Int, val fileName: String, val fileUri: String)
@Serializable
data class FileRecord(var id: Int, var name: String, var uri: String)
@Serializable
data class RenameFileRequest(val fileId: Int, val newName: String)
@Serializable
data class FolderRequest(val folderName: String)
@Serializable
data class RenameFolderRequest(val folderId: Int, val newName: String)

interface UserDB {
    fun addUser(username: String, password: String): Int
    fun validateUser(username: String, password: String): Boolean
}
interface ReviewDB {
    fun addReview(courseReview: CourseReview): Boolean
    fun getAllReviewsFrom(courseCode: String): List<CourseReview>
}
interface EnrollmentDB {
    fun getAllEnrollments(currentUser: String): MutableList<Event>
    fun addEnrollment(currentUser: String, cs: CourseSchedule, course: Course): Boolean
}
interface FileFolderDB {
    fun addFileToFolder(folderId: Int, fileName: String, fileUri: String): Boolean
    fun deleteFile(fileId: Int): Boolean
    fun getFilesInFolder(folderId: Int): List<FileRecord>
    fun renameFile(fileId: Int, newName: String): Boolean
    fun isFileExistInFolder(folderId: Int, fileName: String): Boolean
    fun addFolder(folderName: String): Boolean
    fun getAllFolders(): List<FolderRecord>
    fun deleteFolder(folderId: Int): Boolean
    fun renameFolder(folderId: Int, newName: String): Boolean
    fun folderNameExists(folderName: String): Boolean
}

@ExtendWith(MockitoExtension::class)
class ApplicationTest {

    private val userDB = mock(UserDB::class.java)
    private val reviewDB = mock(ReviewDB::class.java)
    private val enrollmentDB = mock(EnrollmentDB::class.java)
    private val fileFolderDB = mock(FileFolderDB::class.java)

    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test add user`() = runTest {
        `when`(userDB.addUser("newUser", "newPass")).thenReturn(1)
        `when`(userDB.addUser("existingUser", "existingPass")).thenReturn(2)

        testApplication {
            application {
                configureRouting()
            }

            val responseNewUser = client.post("/users/add") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(UserCredentials("newUser", "newPass")))
            }
            assertEquals(HttpStatusCode.OK, responseNewUser.status)
            assertEquals("User added successfully", responseNewUser.bodyAsText())

            val responseExistingUser = client.post("/users/add") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(UserCredentials("existingUser", "existingPass")))
            }
            assertEquals(HttpStatusCode.OK, responseExistingUser.status)
            assertEquals("Username already exists", responseExistingUser.bodyAsText())
        }
    }

    @Test
    fun `test validate user`() = runTest {
        `when`(userDB.validateUser("validUser", "validPass")).thenReturn(true)
        `when`(userDB.validateUser("invalidUser", "invalidPass")).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val responseValidUser = client.post("/validate") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(UserCredentials("validUser", "validPass")))
            }
            assertEquals(HttpStatusCode.OK, responseValidUser.status)
            assertEquals("User successfully validated", responseValidUser.bodyAsText())

            val responseInvalidUser = client.post("/validate") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(UserCredentials("invalidUser", "invalidPass")))
            }
            assertEquals(HttpStatusCode.InternalServerError, responseInvalidUser.status)
            assertEquals("User not validated", responseInvalidUser.bodyAsText())
        }
    }

    @Test
    fun `test add review`() = runTest {
        val validReview = CourseReview("user123", "CS101", "2021-05-01", "Great course!", 5)
        `when`(reviewDB.addReview(validReview)).thenReturn(true)

        val invalidReview = CourseReview("user123", "CS999", "2021-05-01", "Terrible course!", 1)
        `when`(reviewDB.addReview(invalidReview)).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val responseValidReview = client.post("/reviews/add") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(validReview))
            }
            assertEquals(HttpStatusCode.OK, responseValidReview.status)
            assertEquals("Review successfully added", responseValidReview.bodyAsText())

            val responseInvalidReview = client.post("/reviews/add") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(invalidReview))
            }
            assertEquals(HttpStatusCode.InternalServerError, responseInvalidReview.status)
            assertEquals("Review not added", responseInvalidReview.bodyAsText())
        }
    }

    @Test
    fun `test get reviews by course code`() = runTest {
        val courseCode = "CS135"
        val reviews = listOf(
            CourseReview("user123", courseCode, "2021-05-01", "Great course!", 5),
            CourseReview("user456", courseCode, "2021-06-01", "Loved it!", 4)
        )
        `when`(reviewDB.getAllReviewsFrom(courseCode)).thenReturn(reviews)

        testApplication {
            application {
                configureRouting()
            }

            val response = client.get("/reviews/$courseCode")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(Json.encodeToString(reviews), response.bodyAsText())

            val badResponse = client.get("/reviews/")
            assertEquals(HttpStatusCode.NotFound, badResponse.status)
        }
    }

    @Test
    fun `test get enrollments by user`() = runTest {
        val currentUser = "john_doe"
        val enrollments = mutableListOf(
            Event("CS101", LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Lecture"),
            Event("MATH123", LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Tutorial")
        )
        `when`(enrollmentDB.getAllEnrollments(currentUser)).thenReturn(enrollments)

        testApplication {
            application {
                configureRouting()
                configureSerialization()
            }

            val response = client.get("/enrollments/$currentUser")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(Json.encodeToString(enrollments), response.bodyAsText())

            val badResponse = client.get("/enrollments/")
            assertEquals(HttpStatusCode.NotFound, badResponse.status)
        }
    }

    @Test
    fun `test add enrollment`() = runTest {
        val currentUser = "john_doe"
        val cs = CourseSchedule("Section A", 25, 30, "08:00", "10:00", "MWF")
        val course = Course("CS101", "Intro to CS", "Basics of Computer Science")
        `when`(enrollmentDB.addEnrollment(currentUser, cs, course)).thenReturn(true)

        testApplication {
            application {
                configureRouting()
            }

            val response = client.post("/enrollments/addEnrollment") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(EnrollmentRequest(currentUser, cs, course)))
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Enrollment added successfully", response.bodyAsText())
        }
    }

    @Test
    fun `test add file to folder`() = runTest {
        val successfulRequest = FileToFolderRequest(1, "testFile.txt", "file://testFile.txt")
        `when`(fileFolderDB.addFileToFolder(successfulRequest.folderId, successfulRequest.fileName, successfulRequest.fileUri)).thenReturn(true)
        val failedRequest = FileToFolderRequest(2, "testFile.txt", "file://testFile.txt")
        `when`(fileFolderDB.addFileToFolder(failedRequest.folderId, failedRequest.fileName, failedRequest.fileUri)).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val successResponse = client.post("/fileFolder/addFileToFolder") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(successfulRequest))
            }
            assertEquals(HttpStatusCode.OK, successResponse.status)
            assertEquals("file added successfully", successResponse.bodyAsText())

            val failureResponse = client.post("/fileFolder/addFileToFolder") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(failedRequest))
            }
            assertEquals(HttpStatusCode.InternalServerError, failureResponse.status)
            assertEquals("Failed to add file", failureResponse.bodyAsText())
        }
    }

    @Test
    fun `test delete file`() = runTest {
        `when`(fileFolderDB.deleteFile(1)).thenReturn(true)
        `when`(fileFolderDB.deleteFile(2)).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val successResponse = client.delete("/fileFolder/deleteFile/1")
            assertEquals(HttpStatusCode.OK, successResponse.status)
            assertEquals("file deleted successfully", successResponse.bodyAsText())

            val failureResponse = client.delete("/fileFolder/deleteFile/2")
            assertEquals(HttpStatusCode.InternalServerError, failureResponse.status)
            assertEquals("Failed to delete file", failureResponse.bodyAsText())

            val badRequestResponse = client.delete("/fileFolder/deleteFile/invalid")
            assertEquals(HttpStatusCode.BadRequest, badRequestResponse.status)
        }
    }

    @Test
    fun `test get files in folder`() = runTest {
        val fileList = listOf(FileRecord(1, "testFile1.txt", "file://testFile1.txt"))
        `when`(fileFolderDB.getFilesInFolder(1)).thenReturn(fileList)
        `when`(fileFolderDB.getFilesInFolder(2)).thenReturn(emptyList())

        testApplication {
            application {
                configureRouting()
            }

            val successResponse = client.get("/fileFolder/getFilesInFolder/1")
            assertEquals(HttpStatusCode.OK, successResponse.status)
            assertEquals(Json.encodeToString(fileList), successResponse.bodyAsText())

            val emptyResponse = client.get("/fileFolder/getFilesInFolder/2")
            assertEquals(HttpStatusCode.OK, emptyResponse.status)
            assertEquals("[]", emptyResponse.bodyAsText())

            val badRequestResponse = client.get("/fileFolder/getFilesInFolder/invalid")
            assertEquals(HttpStatusCode.BadRequest, badRequestResponse.status)
        }
    }

    @Test
    fun `test rename file`() = runTest {
        `when`(fileFolderDB.renameFile(1, "newName.txt")).thenReturn(true)
        `when`(fileFolderDB.renameFile(2, "newName.txt")).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val successResponse = client.post("/fileFolder/renameFile") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RenameFileRequest(1, "newName.txt")))
            }
            assertEquals(HttpStatusCode.OK, successResponse.status)
            assertEquals("file renamed successfully", successResponse.bodyAsText())

            val failureResponse = client.post("/fileFolder/renameFile") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RenameFileRequest(2, "newName.txt")))
            }
            assertEquals(HttpStatusCode.InternalServerError, failureResponse.status)
            assertEquals("Failed to rename file", failureResponse.bodyAsText())
        }
    }


    @Test
    fun `test file existence in folder`() = runTest {
        `when`(fileFolderDB.isFileExistInFolder(1, "existingFile.txt")).thenReturn(true)
        `when`(fileFolderDB.isFileExistInFolder(2, "nonExistingFile.txt")).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val fileExistsResponse = client.get("/fileFolder/isFileExistInFolder/1/existingFile.txt")
            assertEquals(HttpStatusCode.OK, fileExistsResponse.status)
            assertEquals("file exists", fileExistsResponse.bodyAsText())

            val fileDoesNotExistResponse = client.get("/fileFolder/isFileExistInFolder/2/nonExistingFile.txt")
            assertEquals(HttpStatusCode.InternalServerError, fileDoesNotExistResponse.status)
            assertEquals("file does not exist", fileDoesNotExistResponse.bodyAsText())

            val badRequestResponse = client.get("/fileFolder/isFileExistInFolder/invalid/nonExistingFile.txt")
            assertEquals(HttpStatusCode.BadRequest, badRequestResponse.status)
        }
    }

    @Test
    fun `test add folder`() = runTest {
        `when`(fileFolderDB.addFolder("NewFolder")).thenReturn(true)
        `when`(fileFolderDB.addFolder("ExistingFolder")).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val successResponse = client.post("/fileFolder/addFolder") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(FolderRequest("NewFolder")))
            }
            assertEquals(HttpStatusCode.OK, successResponse.status)
            assertEquals("folder added successfully", successResponse.bodyAsText())

            val failureResponse = client.post("/fileFolder/addFolder") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(FolderRequest("ExistingFolder")))
            }
            assertEquals(HttpStatusCode.InternalServerError, failureResponse.status)
            assertEquals("Failed to add folder", failureResponse.bodyAsText())
        }
    }

    @Test
    fun `test get all folders`() = runTest {
        val folderList = listOf(FolderRecord(1, "Folder1"), FolderRecord(2, "Folder2"))
        `when`(fileFolderDB.getAllFolders()).thenReturn(folderList)
        `when`(fileFolderDB.getAllFolders()).thenReturn(emptyList())

        testApplication {
            application {
                configureRouting()
            }

            val successResponse = client.get("/fileFolder/getAllFolders")
            assertEquals(HttpStatusCode.OK, successResponse.status)
            assertEquals(Json.encodeToString(folderList), successResponse.bodyAsText())

            val emptyResponse = client.get("/fileFolder/getAllFolders")
            assertEquals(HttpStatusCode.OK, emptyResponse.status)
            assertEquals("[]", emptyResponse.bodyAsText())
        }
    }

    @Test
    fun `test delete folder`() = runTest {
        `when`(fileFolderDB.deleteFolder(1)).thenReturn(true)
        `when`(fileFolderDB.deleteFolder(2)).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val successResponse = client.delete("/fileFolder/deleteFolder/1")
            assertEquals(HttpStatusCode.OK, successResponse.status)
            assertEquals("folder deleted successfully", successResponse.bodyAsText())

            val failureResponse = client.delete("/fileFolder/deleteFolder/2")
            assertEquals(HttpStatusCode.InternalServerError, failureResponse.status)
            assertEquals("Failed to delete folder", failureResponse.bodyAsText())

            val badRequestResponse = client.delete("/fileFolder/deleteFolder/invalid")
            assertEquals(HttpStatusCode.BadRequest, badRequestResponse.status)
        }
    }

    @Test
    fun `test rename folder`() = runTest {
        `when`(fileFolderDB.renameFolder(1, "NewFolder")).thenReturn(true)
        `when`(fileFolderDB.renameFolder(2, "ExistingFolder")).thenReturn(false)

        testApplication {
            application {
                configureRouting()
            }

            val successResponse = client.post("/fileFolder/renameFolder") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RenameFolderRequest(1, "NewFolder")))
            }
            assertEquals(HttpStatusCode.OK, successResponse.status)
            assertEquals("folder renamed successfully", successResponse.bodyAsText())

            val failureResponse = client.post("/fileFolder/renameFolder") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RenameFolderRequest(2, "ExistingFolder")))
            }
            assertEquals(HttpStatusCode.InternalServerError, failureResponse.status)
            assertEquals("Failed to rename folder", failureResponse.bodyAsText())
        }
    }

    @Test
    fun `test folder name exists`() = runTest {
        `when`(fileFolderDB.folderNameExists("ExistingFolder")).thenReturn(true) // Folder exists
        `when`(fileFolderDB.folderNameExists("NonExistingFolder")).thenReturn(false) // Folder does not exist

        testApplication {
            application {
                configureRouting()
            }

            val existsResponse = client.get("/fileFolder/folderNameExists/ExistingFolder")
            assertEquals(HttpStatusCode.OK, existsResponse.status)
            assertEquals("folder exists", existsResponse.bodyAsText())

            val doesNotExistResponse = client.get("/fileFolder/folderNameExists/NonExistingFolder")
            assertEquals(HttpStatusCode.InternalServerError, doesNotExistResponse.status)
            assertEquals("folder does not exist", doesNotExistResponse.bodyAsText())

            val badRequestResponse = client.get("/fileFolder/folderNameExists/")
            assertEquals(HttpStatusCode.BadRequest, badRequestResponse.status)
        }
    }
}
