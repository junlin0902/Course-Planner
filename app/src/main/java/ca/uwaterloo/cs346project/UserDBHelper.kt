package ca.uwaterloo.cs346project

import io.ktor.http.encodeURLParameter
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


interface ResponseCallback {
    fun onSuccess(responseBody: String)
    fun onFailure(e: IOException)
}

@Serializable
data class FileRecord(var id: Int, var name: String, var uri: String)
@Serializable
data class FolderRecord(var id: Int, var name: String)
@Serializable
data class EnrollmentRequest(val currentUser: String, val cs: CourseSchedule, val course: Course)


class UserDBHelper{

    companion object {
        private const val URL = "10.0.2.2"
    }

    private fun createOkHttpCallback(callback: ResponseCallback): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onFailure(IOException("Unexpected code $response"))
                    } else {
                        response.body?.string()?.let {
                            callback.onSuccess(it)
                        } ?: callback.onFailure(IOException("Response body is null"))
                    }
                }
            }
        }
    }

    fun addUser(username: String, password: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"username\":\"$username\", \"password\":\"$password\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$URL:8080/users/add")
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }


    fun validateUser(username: String, password: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"username\":\"$username\", \"password\":\"$password\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$URL:8080/users/validate")
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun changePassword(username: String, oldPassword: String, newPassword: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"username\":\"$username\", \"oldPassword\":\"$oldPassword\", \"newPassword\":\"$newPassword\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$URL:8080/users/changePassword")
            .put(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun addReview(courseReview: CourseReview, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"reviewer\":\"${courseReview.reviewer}\", \"courseCode\":\"${courseReview.courseCode}\"," +
                "\"date\":\"${courseReview.date}\", \"stars\":\"${courseReview.stars}\"," +
                "\"content\":\"${courseReview.content}\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$URL:8080/reviews/add")
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun getAllReviewsFrom(courseCode: String, callback: (List<CourseReview>?, IOException?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$URL:8080/reviews/$courseCode")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val reviews = Json.decodeFromString<List<CourseReview>>(responseBody)
                                callback(reviews, null)
                            } catch (e: SerializationException) {
                                callback(null, IOException("Serialization error: ${e.message}"))
                            } catch (e: Exception) {
                                callback(null, IOException("General error: ${e.message}"))
                            }
                        } else {
                            callback(null, IOException("Response body is null"))
                        }
                    } else {
                        callback(null, IOException("Unexpected code $response"))
                    }
                }
            }
        })
    }

    fun getAllEnrollments(user: String, callback: (List<Event>?, IOException?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$URL:8080/enrollments/$user")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val enrollments = Json.decodeFromString<List<Event>>(responseBody)
                                callback(enrollments, null)
                            } catch (e: Exception) {
                                callback(null, IOException("Error parsing JSON: ${e.message}"))
                            }
                        } else {
                            callback(null, IOException("Response body is null"))
                        }
                    } else {
                        callback(null, IOException("Unexpected code $response"))
                    }
                }
            }
        })
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    fun addEnrollment(currentUser: String, cs: CourseSchedule, course: Course, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody =
            Json.encodeToString(EnrollmentRequest(currentUser, cs, course)).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$URL:8080/enrollments/addEnrollment")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun addFileToFolder(folderId: Int, fileName: String, fileUri: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"folderId\":\"$folderId\", \"fileName\":\"$fileName\", \"fileUri\":\"$fileUri\"}"
        val body = json.toRequestBody(mediaType)
        val httpRequest = Request.Builder()
            .url("http://$URL:8080/fileFolder/addFileToFolder")
            .post(body)
            .build()
        client.newCall(httpRequest).enqueue(createOkHttpCallback(callback))
    }

    fun deleteFile(fileId: Int, callback: ResponseCallback) {
        val client = OkHttpClient()
        val httpRequest = Request.Builder()
            .url("http://$URL:8080/fileFolder/deleteFile/$fileId")
            .delete()
            .build()
        client.newCall(httpRequest).enqueue(createOkHttpCallback(callback))
    }

    fun getFilesInFolder(folderId: Int, callback: (List<FileRecord>?, IOException?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$URL:8080/fileFolder/getFilesInFolder/$folderId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val files = Json.decodeFromString<List<FileRecord>>(responseBody)
                                callback(files, null)
                            } catch (e: Exception) {
                                callback(null, IOException("Error parsing JSON: ${e.message}"))
                            }
                        } else {
                            callback(null, IOException("Response body is null"))
                        }
                    } else {
                        callback(null, IOException("Unexpected code $response"))
                    }
                }
            }
        })
    }

    fun renameFile(fileId: Int, newName: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"fileId\":\"$fileId\", \"newName\":\"$newName\"}"
        val body = json.toRequestBody(mediaType)
        val httpRequest = Request.Builder()
            .url("http://$URL:8080/fileFolder/renameFile")
            .post(body)
            .build()
        client.newCall(httpRequest).enqueue(createOkHttpCallback(callback))
    }

    fun isFileExistInFolder(folderId: Int, fileName: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val httpRequest = Request.Builder()
            .url("http://$URL:8080/fileFolder/isFileExistInFolder/$folderId/${fileName.encodeURLParameter()}")
            .get()
            .build()
        client.newCall(httpRequest).enqueue(createOkHttpCallback(callback))
    }

    fun addFolder(folderName: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"folderName\":\"$folderName\"}"
        val body = json.toRequestBody(mediaType)
        val httpRequest = Request.Builder()
            .url("http://$URL:8080/fileFolder/addFolder")
            .post(body)
            .build()
        client.newCall(httpRequest).enqueue(createOkHttpCallback(callback))
    }

    fun getAllFolders(callback: (List<FolderRecord>?, IOException?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$URL:8080/fileFolder/getAllFolders")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val folders = Json.decodeFromString<List<FolderRecord>>(responseBody)
                                callback(folders, null)
                            } catch (e: Exception) {
                                callback(null, IOException("Error parsing JSON: ${e.message}"))
                            }
                        } else {
                            callback(null, IOException("Response body is null"))
                        }
                    } else {
                        callback(null, IOException("Unexpected code $response"))
                    }
                }
            }
        })
    }

    fun deleteFolder(folderId: Int, callback: ResponseCallback) {
        val client = OkHttpClient()
        val httpRequest = Request.Builder()
            .url("http://$URL:8080/fileFolder/deleteFolder/$folderId")
            .delete()
            .build()
        client.newCall(httpRequest).enqueue(createOkHttpCallback(callback))
    }

    fun renameFolder(folderId: Int, newName: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"folderId\":\"$folderId\", \"newName\":\"$newName\"}"
        val body = json.toRequestBody(mediaType)
        val httpRequest = Request.Builder()
            .url("http://$URL:8080/fileFolder/renameFolder")
            .post(body)
            .build()
        client.newCall(httpRequest).enqueue(createOkHttpCallback(callback))
    }

    fun folderNameExists(name: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val httpRequest = Request.Builder()
            .url("http://$URL:8080/fileFolder/folderNameExists/${name.encodeURLParameter()}")
            .get()
            .build()
        client.newCall(httpRequest).enqueue(createOkHttpCallback(callback))
    }
}