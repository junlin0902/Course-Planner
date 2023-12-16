package ca.uwaterloo.cs346project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import java.io.IOException

class CourseMaterialFolder : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDBHelper = UserDBHelper()
        setContent {
            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CourseMaterialFolderPage(userDBHelper)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseMaterialFolderPage(userDBHelper: UserDBHelper) {
    var folderList by remember { mutableStateOf(listOf<FolderRecord>()) }
    var selectedFolderForRename by remember { mutableStateOf<FolderRecord?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderRecord?>(null) }
    var folderToEnter by remember { mutableStateOf<FolderRecord?>(null) }
    val scrollState = rememberScrollState()
    var showAddFolderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userDBHelper.getAllFolders() { allFolders, error ->
            if (error != null) {
                println("Error fetching folders: ${error.message}")
            } else if (allFolders != null) {
                println("initial fetch of all folders")
                folderList = allFolders
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showAddFolderDialog = true
            }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Course Material Folder",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (folderList.isEmpty()) {
                Text(
                    text = "No course material folder has been added.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 20.dp)
                )
            } else {
                folderList.forEach { folder ->
                    FolderItem(
                        folderName = folder.name,
                        onFolderClick = {folderToEnter = folder},
                        onDelete = { folderToDelete = folder },
                        onRename = { selectedFolderForRename = folder }
                    )
                }
            }
        }
    }

    if (showAddFolderDialog) {
        AddFolderDialog(
            onAddFolder = { folderName ->
                userDBHelper.addFolder(folderName, object: ResponseCallback {
                    override fun onSuccess(responseBody: String) {
                        println("folder added")
                        userDBHelper.getAllFolders() { allFolders, error ->
                            if (error != null) {
                                println("Error fetching folders: ${error.message}")
                            } else if (allFolders != null) {
                                println("updated fetch of all files, after adding folder")
                                folderList = allFolders
                            }
                        }
                    }

                    override fun onFailure(e: IOException) {
                        println("folder not added")
                        e.printStackTrace()
                    }
                })
                showAddFolderDialog = false
            },
            onDismiss = {
                showAddFolderDialog = false
            }
        )
    }

    if (folderToEnter != null) {
        val context = LocalContext.current
        val CMIntent = Intent(context, CourseMaterial::class.java)
        CMIntent.putExtra("FOLDER_ID", folderToEnter!!.id)
        CMIntent.putExtra("FOLDER_NAME", folderToEnter!!.name)
        context.startActivity(CMIntent)
        folderToEnter = null
    }

    if (selectedFolderForRename != null) {
        RenameFolderDialog(
            initialName = selectedFolderForRename!!.name,
            onRename = { newName ->
                userDBHelper.renameFolder(selectedFolderForRename!!.id, newName, object: ResponseCallback {
                    override fun onSuccess(responseBody: String) {
                        println("folder renamed")
                        selectedFolderForRename = null
                        userDBHelper.getAllFolders() { allFolders, error ->
                            if (error != null) {
                                println("Error fetching folders: ${error.message}")
                            } else if (allFolders != null) {
                                println("updated fetch of all folders, after folder renaming")
                                folderList = allFolders
                            }
                        }
                    }

                    override fun onFailure(e: IOException) {
                        println("folder not renamed")
                        e.printStackTrace()
                    }
                })
            },
            onDismiss = { selectedFolderForRename = null }
        )
    }

    if (folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete \"${folderToDelete?.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    userDBHelper.deleteFolder(folderToDelete!!.id, object: ResponseCallback {
                        override fun onSuccess(responseBody: String) {
                            println("folder deleted")

                            userDBHelper.getFilesInFolder(folderToDelete!!.id) { allFiles, error ->
                                if (error != null) {
                                    println("Error fetching folders: ${error.message}")
                                } else if (allFiles != null) {
                                    println("updated fetch of all folders, after folder delete")
                                    for (file in allFiles) {
                                        userDBHelper.deleteFile(file.id, object: ResponseCallback {
                                            override fun onSuccess(responseBody: String) {
                                                println("file deleted")
                                            }

                                            override fun onFailure(e: IOException) {
                                                println("file not deleted")
                                                e.printStackTrace()
                                            }
                                        })
                                    }
                                }
                            }

                            userDBHelper.getAllFolders() { allFolders, error ->
                                if (error != null) {
                                    println("Error fetching folders: ${error.message}")
                                } else if (allFolders != null) {
                                    println("updated fetch of all folders, after folder delete")
                                    folderList = allFolders
                                }
                            }
                            folderToDelete = null
                        }

                        override fun onFailure(e: IOException) {
                            println("folder not deleted")
                            e.printStackTrace()
                        }
                    })
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun FolderItem(folderName: String, onFolderClick: () -> Unit, onDelete: () -> Unit, onRename: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {onFolderClick()}
        ) {
            Text(text = folderName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRename) {
                Icon(Icons.Default.Edit, contentDescription = "Rename")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderDialog(
    onAddFolder: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newFolderName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String>("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Folder") },
        text = {
            TextField(
                value = newFolderName,
                onValueChange = {
                    newFolderName = it
                    errorMessage = "" },
                label = { Text("Folder Name    $errorMessage") },
                isError = errorMessage != ""
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val userDBHelper = UserDBHelper()
                    userDBHelper.folderNameExists(newFolderName, object: ResponseCallback {
                        override fun onSuccess(responseBody: String) {
                            errorMessage = "name already exists."
                            println("$newFolderName exists")
                        }

                        override fun onFailure(e: IOException) {
                            onAddFolder(newFolderName)
                            println("$newFolderName does not exist")
                            newFolderName = ""
                            e.printStackTrace()
                        }
                    })
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameFolderDialog(initialName: String, onRename: (String) -> Unit, onDismiss: () -> Unit) {
    var newName by remember { mutableStateOf(initialName) }
    val maxLength = 20

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Folder") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { if (it.length <= maxLength) newName = it },
                label = { Text("New Name") },
                singleLine = true,
                isError = newName.length > maxLength
            )
        },
        confirmButton = {
            TextButton(onClick = { onRename(newName) }) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



