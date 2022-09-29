package com.example.cmsclonelite.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.AnnouncementsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun AddAnnouncementsScreen(navController: NavHostController, course: Course, announcementsViewModel: AnnouncementsViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    mAuth = FirebaseAuth.getInstance()
    val title by announcementsViewModel.title.observeAsState("")
    val body by announcementsViewModel.body.observeAsState("")
    val showAnnouncementAddDialog: Boolean by announcementsViewModel.isAddAnnouncementDialog.observeAsState(false)
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
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddAnnouncementsScreenPreview() {
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance()
    val courseRepository = CourseRepository()
    val announcementsViewModel = AnnouncementsViewModel(db, mAuth, courseRepository)
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