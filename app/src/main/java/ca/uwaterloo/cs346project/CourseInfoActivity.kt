package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import kotlinx.serialization.Serializable
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class CourseReview(val reviewer: String, val courseCode: String, val date: String, val content: String, val stars: Int)

val noOfferings: String = "Course not offered this term."

class CourseInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentlyLoggedInUser = intent.getStringExtra("CURRENT_USER") ?: ""
        println(currentlyLoggedInUser)
        setContent {
            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Extract course information from intent extras
                    val courseCode = intent.getStringExtra("COURSE_CODE") ?: ""
                    val courseName = intent.getStringExtra("COURSE_NAME") ?: ""
                    val courseDescription = intent.getStringExtra("COURSE_DESCRIPTION") ?: ""
                    var courseOfferings: List<CourseSchedule>
                    try {
                        courseOfferings = UWAPIHelper.getCourseScheduleData(courseCode)
                    } catch (e: Exception) {
                        println("error")
                        courseOfferings = listOf(CourseSchedule(noOfferings, 0, 0, "", "", ""))
                    }

                    CourseInfoScreen(
                        currentlyLoggedInUser, courseCode, courseName, courseDescription,
                        courseOfferings, onBackButtonClick = { navigateBackToMainActivity(currentlyLoggedInUser) }
                    )
                }
            }
            BackHandler {
                navigateBackToMainActivity(currentlyLoggedInUser)
            }
        }
    }
    private fun navigateBackToMainActivity(currentlyLoggedInUser: String) {
        // Create an Intent to navigate back to MainActivity
        val intent = Intent(this, HomePageActivity::class.java)
        intent.putExtra("CURRENT_USER", currentlyLoggedInUser)

        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CourseInfoScreen(
    currentlyLoggedInUser: String,
    courseCode: String,
    courseName: String,
    courseDescription: String,
    courseOfferings: List<CourseSchedule>,
    onBackButtonClick: () -> Unit,
) {
    var showToast by remember { mutableStateOf(false) }
    var allReviews by remember { mutableStateOf<List<CourseReview>?>(null) }

    MaterialTheme (
        typography = Typography(),
        shapes = Shapes()
    ) {
        val dbHelper = UserDBHelper()
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            var isDialogOpen by remember { mutableStateOf(false) }

            Text(
                text = "$courseCode: $courseName",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = courseDescription, style = MaterialTheme.typography.bodySmall)
//            Text(
//                text = "Instructor: $instructorName",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.secondary,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )

            Text(
                text = "Course Offerings:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (showToast) {
                Toast("Course added!") {
                    // Dismiss the toast when clicked
                    showToast = false
                }
            }

            val screenHeight = LocalConfiguration.current.screenHeightDp.dp

            LazyColumn(
                modifier = Modifier
                    .heightIn(max = screenHeight * 0.4f) // Half of the screen height
                    .fillMaxWidth()
            ) {
                itemsIndexed(courseOfferings) { index, offering ->
                    val text: String
                    if (offering.section == noOfferings) {
                        text = noOfferings
                    } else {
                        text = "Enrollment: ${offering.enrollment} / ${offering.maxEnrollment}, \t ${offering.meetDays}: ${offering.meetStart.substring(11, 16)} - ${offering.meetEnd.substring(11, 16)}"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val course = Course(courseCode, courseName, courseDescription)
                        AddCourseButton(currentlyLoggedInUser, offering, course) {
                            // Set the state to show the toast
                            showToast = true
                        }
                        Text(text = offering.section, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Add a divider between rows except for the last row
                    if (index < courseOfferings.size - 1) {
                        Divider(modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp))
                    }
                }
            }

            Button(
                onClick = { isDialogOpen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Add a Review for $courseCode")
            }

//          Check if the dialog should be open
            if (isDialogOpen) {
                ReviewDialog(
                    onDismiss = { isDialogOpen = false },
                    onSubmitReview = { reviewContent, rating ->
                        val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
                        val newReview = CourseReview(currentlyLoggedInUser, courseCode, currentDate, reviewContent, rating)
                        dbHelper.addReview(newReview, object : ResponseCallback {
                            override fun onSuccess(responseBody: String) {
                                println("review added")
                                dbHelper.getAllReviewsFrom(courseCode) { reviews, error ->
                                    if (error != null) {
                                        println("Error fetching reviews: ${error.message}")
                                    } else if (reviews != null) {
                                        allReviews = reviews
                                    }
                                }
                            }
                            override fun onFailure(e: IOException) {
                                println("failed to add review")
                            }
                        })

                    }
                )
            }

            dbHelper.getAllReviewsFrom(courseCode) { reviews, error ->
                if (error != null) {
                    println("Error fetching reviews: ${error.message}")
                } else if (reviews != null) {
                    allReviews = reviews
                }
            }

            if (allReviews != null) {
                // Display the reviews
                CourseReviews(allReviews!!)
            } else {
                // Display a loading indicator or a message
                Text("Loading reviews...")
            }


            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally) // Align the box to the bottom
            ) {
                Button(
                    onClick = { onBackButtonClick() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    Text("Back to Main Screen")
                }
            }
        }
    }
}

@Composable
fun AddCourseButton(currUser: String, cs: CourseSchedule, course: Course, onClick: () -> Unit) {
    // Material Design button that triggers the custom toast when clicked
    val dbHelper = UserDBHelper()
    Button(
        onClick = {
            onClick()
            dbHelper.addEnrollment(currUser, cs, course, object: ResponseCallback {
                override fun onSuccess(responseBody: String) {
                    println("Successfully added enrollment")
                }

                override fun onFailure(e: IOException) {
                    println("Enrollment not added")
                }
            })
        },
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = "Add to calendar",
            fontSize = 12.sp
        )
    }
}
@Composable
fun Toast(message: String, onDismiss: () -> Unit) {
    // A custom toast-like composable
    Card(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                // Call the onDismiss function when the toast is clicked
                onDismiss()
            }
    ) {
        Text(message, modifier = Modifier.padding(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseReviews(reviews: List<CourseReview>) {
    LazyColumn (
        modifier = Modifier.fillMaxHeight(0.85f)
    ){
        itemsIndexed(reviews) { _, review ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = review.reviewer, fontWeight = FontWeight.Bold)
                    Text(text = review.date)
                    RatingBar(
                        rating = review.stars
                    )
                    Text(text = review.content)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    onDismiss: () -> Unit,
    onSubmitReview: (String, Int) -> Unit,
) {
    var reviewContent by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add a Review") },
        text = {
            Column {
                RatingBar(
                    rating = rating,
                    isInteractable = true,
                    onRatingChanged = { newRating ->
                        rating = newRating
                    }
                )
//                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = reviewContent,
                    onValueChange = { reviewContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Review") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reviewContent.isNotEmpty()) {
                        onSubmitReview(reviewContent, rating)
                        reviewContent = ""
                        onDismiss()
                    }
                }
            ) {
                Text(text = "Submit")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun RatingBar(
    rating: Int,
    isInteractable: Boolean = false,
    onRatingChanged: (Int) -> Unit = {}
) {
    Row {
        val starIcon = painterResource(id = R.drawable.baseline_star_24)
        val starOutlineIcon = painterResource(id = R.drawable.baseline_star_border_24)
        for (index in 0 until 5) {
            Icon(
                painter = if (index < rating) starIcon else starOutlineIcon,
                contentDescription = null, // Decorative element
                tint = Color.Black,
                modifier = Modifier
                    .height(24.dp)
                    .padding(end = 4.dp)
                    .clickable(enabled = isInteractable) {
                        if (isInteractable) {
                            val newRating = index + 1
                            onRatingChanged(newRating)
                        }
                    }
            )
        }
    }
}

