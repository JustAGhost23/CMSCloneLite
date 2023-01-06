package com.example.cmsclonelite.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.AnnouncementsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storageMetadata


private lateinit var mAuth: FirebaseAuth

//TODO: Make Progress indicator if possible

@Composable
fun AddAnnouncementsScreen(
    navController: NavHostController,
    course: Course,
    announcementsViewModel: AnnouncementsViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    mAuth = FirebaseAuth.getInstance()
    val title by announcementsViewModel.title.observeAsState("")
    val body by announcementsViewModel.body.observeAsState("")
    val showAnnouncementAddDialog: Boolean by announcementsViewModel.isAddAnnouncementDialog.observeAsState(false)
    val showFileDeleteDialog: Boolean by announcementsViewModel.isFileDeleteDialog.observeAsState(false)
    val fileUri: Uri? by announcementsViewModel.fileUri.observeAsState(null)
    val downloadUri: Uri? by announcementsViewModel.downloadUri.observeAsState(null)
    val fileMetadata: StorageMetadata by announcementsViewModel.storageMetadata.observeAsState(storageMetadata {})
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.type = "*/*"
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    val uriHandler = LocalUriHandler.current
    val intentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            announcementsViewModel.setFileUri(activityResult.data?.data)
            announcementsViewModel.uploadFileToFirebase(course, context)
        }}
    announcementsViewModel.initialize()
    Card {
        if (showAnnouncementAddDialog) {
            AnnouncementAddConfirmation(showDialog = showAnnouncementAddDialog,
                onDismiss = {announcementsViewModel.removeAddAnnouncementDialog()},
                navController = navController,
                course = course,
                context = context,
                announcementsViewModel = announcementsViewModel
            )
        }
    }
    Card {
        if (showFileDeleteDialog) {
            FileDeleteConfirmation(showDialog = showFileDeleteDialog,
                onDismiss = {announcementsViewModel.removeFileDeleteDialog()},
                fileUri = fileUri!!,
                course = course,
                context = context,
                announcementsViewModel = announcementsViewModel
            )
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Announcement") },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigateUp()
                        }) {
                            Icon(Icons.Rounded.ArrowBack, "")
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    announcementsViewModel.showAddAnnouncementDialog()
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Announcement (Admin Only)")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Spacer(modifier = Modifier.padding(top = 50.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    value = title,
                    colors = TextFieldDefaults.textFieldColors(),
                    label = { Text("Title") },
                    placeholder = { Text("Enter Title") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    onValueChange = {
                        announcementsViewModel.setTitle(it)
                    })
                Spacer(modifier = Modifier.padding(top = 20.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.3f),
                    value = body,
                    colors = TextFieldDefaults.textFieldColors(),
                    label = { Text("Body") },
                    placeholder = { Text("Enter Body") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    onValueChange = {
                        announcementsViewModel.setBody(it)
                    })
                Spacer(modifier = Modifier.padding(top = 20.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 4.dp)
                        .clickable(
                            onClick = {
                                intentLauncher.launch(intent)
                            }
                        ),
                    elevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom  = 16.dp)
                        ) {
                            Text(
                                "Upload a file",
                                fontSize = 16.sp,
                            )
                            Spacer(Modifier.fillMaxWidth(0.85f))
                            Icon(Icons.Default.Add, contentDescription = "Upload a file")
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(top = 40.dp))
                if(downloadUri != null && fileMetadata.name != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable(
                                    onClick = {
                                        uriHandler.openUri(downloadUri.toString())
                                    }
                                ),
                            text = fileMetadata.name!!,
                            fontSize = 20.sp
                        )
                        IconButton(
                            onClick = {
                                announcementsViewModel.showFileDeleteDialog()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Delete file"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddAnnouncementsScreenPreview() {
    val db = FirebaseFirestore.getInstance()
    val courseRepository = CourseRepository()
    val announcementsViewModel = AnnouncementsViewModel(db, courseRepository)
    AddAnnouncementsScreen(rememberNavController(), Course(), announcementsViewModel)
}
@Composable
fun AnnouncementAddConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    course: Course,
    context: Context,
    announcementsViewModel: AnnouncementsViewModel
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Add Announcement")
            },
            text = {
                Text("Are you sure you want to add this announcement?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    announcementsViewModel.postAnnouncement(context, navController, course)
                }) {
                    Text("OK")
                } },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun FileDeleteConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    fileUri: Uri,
    course: Course,
    context: Context,
    announcementsViewModel: AnnouncementsViewModel
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Delete File")
            },
            text = {
                Text("Are you sure you want to delete this file?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    announcementsViewModel.deleteFileFromFirebase(course, fileUri, context)
                    announcementsViewModel.removeFileDeleteDialog()
                }) {
                    Text("OK")
                } },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}