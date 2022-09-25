package com.example.cmsclonelite.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Announcement
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddAnnouncementsScreen(navController: NavHostController, course: Course) {
    val db = FirebaseFirestore.getInstance()
    val courseRepository = CourseRepository()
    val focusManager = LocalFocusManager.current
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
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
                        title = it
                    })
                Spacer(modifier = Modifier.padding(top = 20.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(0.9f)
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
                        body = it
                    })
                Spacer(modifier = Modifier.padding(top = 100.dp))
                Button(
                    onClick = {
                        courseRepository.addAnnouncement(db, course.id!!, Announcement(title, body))
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.MainScreen.route) {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Text(text = "Add Announcement",
                        fontSize = 15.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddAnnouncementsScreenPreview() {
    AddAnnouncementsScreen(rememberNavController(), Course())
}