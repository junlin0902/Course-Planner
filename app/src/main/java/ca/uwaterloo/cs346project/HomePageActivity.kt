package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.getColorScheme

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentlyLoggedInUser = intent.getStringExtra("CURRENT_USER") ?: ""
            HomePage(currentlyLoggedInUser)
        }
    }
}

enum class GoToNext {
    Stay, Schedule, Search, Material, Login
}


@Composable
fun HomePage(currentlyLoggedInUser: String) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)

    var nextPage by remember { mutableStateOf(GoToNext.Stay) }
    var checked by remember { mutableStateOf(MainActivity.settings.darkMode) }
    var changed by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = getColorScheme(true, checked)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Course Planner",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
                Button(
                    onClick = {
                        nextPage = GoToNext.Schedule
                    },
                    modifier = buttonModifier
                ) {
                    Text("Course Schedule", fontSize = 24.sp)
                }

                Button(
                    onClick = {
                        nextPage = GoToNext.Search
                    },
                    modifier = buttonModifier
                ) {
                    Text("Course Search", fontSize = 24.sp)
                }

                Button(
                    onClick = {
                        nextPage = GoToNext.Material
                    },
                    modifier = buttonModifier
                ) {
                    Text("Course Material", fontSize = 24.sp)
                }


                Button(
                    onClick = {
                        nextPage = GoToNext.Login
                    },
                    modifier = buttonModifier
                ) {
                    Text("Log Out", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Toggle Dark Mode",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Switch(
                        checked = checked,
                        onCheckedChange = {
                            checked = it
                            changed = true
                            MainActivity.settings.darkMode = checked
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    if (changed) {
                        WriteSettings()
                        changed = false
                    }
                }
            }
        }
    }

    when (nextPage) {
        GoToNext.Schedule -> {
            val context = LocalContext.current
            val scheduleIntent = Intent(context, ScheduleActivity::class.java)
            scheduleIntent.putExtra("CURRENT_USER", currentlyLoggedInUser)
            context.startActivity(scheduleIntent)
            nextPage = GoToNext.Stay
        }

        GoToNext.Search -> {
            val context = LocalContext.current
            val courseActivityIntent = Intent(context, CourseSearchActivity::class.java)
            courseActivityIntent.putExtra("CURRENT_USER", currentlyLoggedInUser)
            context.startActivity(courseActivityIntent)
            nextPage = GoToNext.Stay
        }
        
        GoToNext.Material -> {
            val context = LocalContext.current
            val courseMaterialIntent = Intent(context, CourseMaterialFolder::class.java)
            context.startActivity(courseMaterialIntent)
            nextPage = GoToNext.Stay
        }

        GoToNext.Login -> {
            LocalContext.current.startActivity(
                Intent(
                    LocalContext.current,
                    LoginActivity::class.java
                )
            )
            nextPage = GoToNext.Stay
        }

        else -> {}
    }
}



