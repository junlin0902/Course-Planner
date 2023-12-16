package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class MainActivity : ComponentActivity() {
    companion object {
        data class UserSettingsData(var darkMode: Boolean = false): java.io.Serializable

        var settings: UserSettingsData = UserSettingsData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadSettings()
            Cs346projectTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }
    }
}

@Composable
fun ReadSettings() {
    val file = File(LocalContext.current.filesDir, "settings.json")
    if (!file.isFile) WriteSettings() // Settings don't exist, write default
    val stream = ObjectInputStream(file.inputStream())

    MainActivity.settings = stream.readObject() as MainActivity.Companion.UserSettingsData
}

@Composable
fun WriteSettings() {
    val file = File(LocalContext.current.filesDir, "settings.json")
    val stream = ObjectOutputStream(file.outputStream())

    stream.writeObject(MainActivity.settings)
}















