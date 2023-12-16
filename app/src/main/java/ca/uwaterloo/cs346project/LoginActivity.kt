package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import java.io.IOException


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginPage()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginPage() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    var Login by remember { mutableStateOf(false) }
    var SignUp by remember { mutableStateOf(false) }
    var CP by remember { mutableStateOf(false) }

    val dbHelper = UserDBHelper()
    CourseList // Initialize CourseList in time for searching
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Username") },
            singleLine = true,
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {
                keyboardController?.hide()
                dbHelper.validateUser(username, password, object : ResponseCallback {
                    override fun onSuccess(responseBody: String) {
                        Login = true
                        errorMessage = ""
                    }
                    override fun onFailure(e: IOException) {
                        errorMessage = "Invalid credentials. Please try again."
                        e.printStackTrace()
                    }
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Login")
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }

        Text(
            text = "Don't have an account? Sign up.",
            color = Color.Blue,
            modifier = Modifier.clickable {
                SignUp = true
            }
        )

        Text(
            text = "Want to Change Password?",
            color = Color.Gray,
            modifier = Modifier.clickable {
                CP = true
            }
        )
    }

    if (Login) {
        val context = LocalContext.current
        val hpIntent = Intent(context, HomePageActivity::class.java)
        hpIntent.putExtra("CURRENT_USER", username)
        context.startActivity(hpIntent)
        Login = false
    }

    if (SignUp) {
        val context = LocalContext.current
        val SignUpIntent = Intent(context, SignUpActivity::class.java)
        context.startActivity(SignUpIntent)
        SignUp = false
    }

    if (CP) {
        val context = LocalContext.current
        val CPIntent = Intent(context, ChangePassword::class.java)
        context.startActivity(CPIntent)
        SignUp = false
    }

}