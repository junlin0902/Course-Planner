package ca.uwaterloo.cs346project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

@Serializable
data class Course(val code: String, val title: String, val description: String)
@Serializable
data class CourseSchedule(val section: String, val enrollment: Int, val maxEnrollment: Int, val meetStart: String, val meetEnd: String, val meetDays: String)

object CourseList: ViewModel() {
    private val courseCodes: MutableList<Course> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                val courseData = JSONArray(UWAPIHelper.apiGet("/v3/Courses/${UWAPIHelper.curTerm}"))
                for (i in 0 until courseData.length()) {
                    val element = courseData.getJSONObject(i)
                    courseCodes.add(
                        Course(element.getString("subjectCode") + element.getString("catalogNumber"),
                            element.getString("title"),
                            element.getString("description"))
                    )
                }
            }
        }
    }

    fun get(): List<Course> {
        return courseCodes
    }
}

object UWAPIHelper {
    var curTerm: String

    init {
        curTerm = apiGet("/v3/Terms/current")?.let { getString(it, "termCode") }.toString()
    }

    ///
    /// HTTP Gets
    ///

    fun apiGet(path: String): String? {
        return runBlocking {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://openapi.data.uwaterloo.ca$path")
                .header("x-api-key", "95808C46CE8F460FA23F6EAC00316045")
                .build()

            try {
                withContext(Dispatchers.IO) { // Dispatch to I/O thread pool
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        response.body?.string() // Safely return the body string
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null // Handle the error case, possibly returning null or a default value
            }
        }
    }

//    fun apiGet(path: String): String {
//        return runBlocking {
//            val client = OkHttpClient()
//            val request = Request.Builder()
//                .url("https://openapi.data.uwaterloo.ca$path")
//                .header("x-api-key", "95808C46CE8F460FA23F6EAC00316045")
//                .build()
//            client.newCall(request).execute().use { response ->
//                return@runBlocking response.body!!.string()
//            }
//        }
//    }

    ///
    /// JSON Gets
    ///

    private fun getString(jsonString: String, key: String): String {
        return JSONObject(jsonString).getString(key)
    }

    private fun getStrings(jsonString: String, keys: List<String>): List<String> {
        val json: JSONObject = JSONObject(jsonString)
        return keys.map { x -> json.getString(x) }
    }

    private inline fun <reified T> getValue(jsonString: String, key: String): T? {
        val ret = JSONObject(jsonString).get(key)
        return if (ret is T) {
            ret
        } else null
    }

    private inline fun <reified T> getValues(jsonString: String, keys: List<String>): List<T?> {
        val json: JSONObject = JSONObject(jsonString)
        return keys.map { key ->
            val value = json.get(key)
            if (value is T) {
                value
            } else null
        }
    }

    ///
    /// Wrapper Methods (Public)
    ///

    fun getCourseScheduleData(courseCode: String): List<CourseSchedule> {
        val numStart: Int = courseCode.toCharArray().indexOfFirst { c -> c.isDigit() }
        val catalogNum: String = courseCode.drop(numStart)
        val subject: String = courseCode.removeSuffix(catalogNum)

        val jsonArr = JSONArray(apiGet("/v3/ClassSchedules/$curTerm/$subject/$catalogNum"))
        val list: MutableList<CourseSchedule> = mutableListOf()

        for (i in 0 until jsonArr.length()) {
            val jsonObject = jsonArr.getJSONObject(i)
            val scheduleObject = jsonObject.getJSONArray("scheduleData").getJSONObject(0)
            list.add(CourseSchedule(
                jsonObject.getString("courseComponent") + " " + jsonObject.getInt("classSection").toString().padStart(3, '0'),
                jsonObject.getInt("enrolledStudents"),
                jsonObject.getInt("maxEnrollmentCapacity"),
                scheduleObject.getString("classMeetingStartTime"),
                scheduleObject.getString("classMeetingEndTime"),
                scheduleObject.getString("classMeetingDayPatternCode")
            ))
        }

        return list.sortedBy { it.section }
    }

}