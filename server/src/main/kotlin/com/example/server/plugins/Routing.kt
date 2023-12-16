package com.example.plugins

import com.example.data.Course
import com.example.data.CourseReview
import com.example.data.CourseSchedule
import com.example.data.EnrollmentDB
import com.example.data.FileFolderDB
import com.example.data.ReviewDB
import com.example.data.UserDB
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {

    val userDB = UserDB()
    val reviewDB = ReviewDB()
    val enrollmentDB = EnrollmentDB()
    val fileFolderDB = FileFolderDB()

    @Serializable
    data class UserCredentials(val username: String, val password: String)

    @Serializable
    data class UserCP(val username: String, val oldPassword: String, val newPassword: String)

    @Serializable
    data class FileToFolderRequest(val folderId: Int, val fileName: String, val fileUri: String)

    @Serializable
    data class RenameFileRequest(val fileId: Int, val newName: String)

    @Serializable
    data class FolderRequest(val folderName: String)

    @Serializable
    data class RenameFolderRequest(val folderId: Int, val newName: String)


    routing {
        get("/") {
            println("ping")
            call.respondText("Hello World!")
        }
        route("/users") {
            post("/add") {
                val userCredentials = call.receive<UserCredentials>()
                when (userDB.addUser(userCredentials.username, userCredentials.password)) {
                    1 -> {
                        call.respondText("User added successfully")
                    }
                    2 -> {
                        call.respondText("Username already exists")
                    }
                    else -> {
                        call.respondText("Failed to add user", status = HttpStatusCode.InternalServerError)
                    }
                }
            }

            post("/validate") {
                val userCredentials = call.receive<UserCredentials>()
                val validated = userDB.validateUser(userCredentials.username, userCredentials.password)
                if (validated) {
                    call.respondText("User successfully validated")
                }
                else {
                    call.respondText("User not validated", status = HttpStatusCode.InternalServerError)
                }
            }

            put("/changePassword") {
                val userCredentials = call.receive<UserCP>()
                when (userDB.changePassword(userCredentials.username, userCredentials.oldPassword, userCredentials.newPassword)) {
                    1 -> {
                        call.respondText("Password changed successfully")
                    }
                    2 -> {
                        call.respondText("Username not exists or false old password")
                    }
                    else -> {
                        call.respondText("Failed", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
        }

        route("/reviews") {
            post("/add") {
                val courseReview = call.receive<CourseReview>()
                val reviewedAdded = reviewDB.addReview(courseReview)
                if (reviewedAdded) {
                    call.respondText("Review successfully added")
                }
                else {
                    call.respondText("Review not added", status = HttpStatusCode.InternalServerError)
                }
            }

            get("/{courseCode}") {
                val courseCode = call.parameters["courseCode"]
                if (courseCode == null) {
                    call.respond(HttpStatusCode.BadRequest, "Course code is required")
                    return@get
                }

                val reviews = reviewDB.getAllReviewsFrom(courseCode)
                call.respond(reviews)
            }
        }

        route("/enrollments") {
            get("/{user}") {
                val user = call.parameters["user"]
                if (user.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "User parameter is missing")
                    return@get
                }

                try {
                    val enrollments = enrollmentDB.getAllEnrollments(user)
                    call.respond(HttpStatusCode.OK, enrollments)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching enrollments: ${e.message}")
                }
            }

            @Serializable
            data class EnrollmentRequest(val currentUser: String, val cs: CourseSchedule, val course: Course)
            post("/addEnrollment") {
                val enrollmentRequest = try {
                    call.receive<EnrollmentRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request format")
                    return@post
                }

                val enrollmentAdded = enrollmentDB.addEnrollment(enrollmentRequest.currentUser, enrollmentRequest.cs, enrollmentRequest.course)
                if (enrollmentAdded) {
                    call.respond(HttpStatusCode.OK, "Enrollment added successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add enrollment")
                }
            }
        }

        route("/fileFolder") {
            post("/addFileToFolder") {
                val request = call.receive<FileToFolderRequest>()
                val result = fileFolderDB.addFileToFolder(request.folderId, request.fileName, request.fileUri)
                if (result) {
                    call.respond(HttpStatusCode.OK, "file added successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add file")
                }
            }

            delete("/deleteFile/{fileId}") {
                val fileId = call.parameters["fileId"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val result = fileFolderDB.deleteFile(fileId)
                if (result) {
                    call.respond(HttpStatusCode.OK, "file deleted successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete file")
                }
            }

            get("/getFilesInFolder/{folderId}") {
                val folderId = call.parameters["folderId"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val fileList = fileFolderDB.getFilesInFolder(folderId)
                call.respond(HttpStatusCode.OK, fileList)
            }

            post("/renameFile") {
                val request = call.receive<RenameFileRequest>()
                val result = fileFolderDB.renameFile(request.fileId, request.newName)
                if (result) {
                    call.respond(HttpStatusCode.OK, "file renamed successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to rename file")
                }
            }

            get("/isFileExistInFolder/{folderId}/{fileName}") {
                val folderId = call.parameters["folderId"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val fileName = call.parameters["fileName"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val exists = fileFolderDB.isFileExistInFolder(folderId, fileName)
                if (exists) {
                    call.respond(HttpStatusCode.OK, "file exists")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "file does not exist")
                }
            }

            post("/addFolder") {
                val folderName = call.receive<FolderRequest>().folderName
                val result = fileFolderDB.addFolder(folderName)
                if (result) {
                    call.respond(HttpStatusCode.OK, "folder added successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add folder")
                }
            }

            get("/getAllFolders") {
                val folders = fileFolderDB.getAllFolders()
                call.respond(HttpStatusCode.OK, folders)
            }

            delete("/deleteFolder/{folderId}") {
                val folderId = call.parameters["folderId"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val result = fileFolderDB.deleteFolder(folderId)
                if (result) {
                    call.respond(HttpStatusCode.OK, "folder deleted successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete folder")
                }
            }

            post("/renameFolder") {
                val request = call.receive<RenameFolderRequest>()
                val result = fileFolderDB.renameFolder(request.folderId, request.newName)
                if (result) {
                    call.respond(HttpStatusCode.OK, "folder renamed successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to rename folder")
                }
            }

            get("/folderNameExists/{folderName}") {
                val folderName = call.parameters["folderName"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val exists = fileFolderDB.folderNameExists(folderName)
                if (exists) {
                    call.respond(HttpStatusCode.OK, "folder exists")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "folder does not exist")
                }
            }
        }
    }
}
