package com.example.cmsclonelite.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditCourseDetailsScreen(navController: NavHostController, course: Course) {
    val focusManager = LocalFocusManager.current
    val db = FirebaseFirestore.getInstance()
    val courseRepository = CourseRepository()
    val showAddDialog = remember { mutableStateOf(false) }
    Card {
        if (showAddDialog.value) {
            CourseAddConfirmation(showDialog = showAddDialog.value,
                onDismiss = {showAddDialog.value = false},
                navController = navController,
                db = db,
                course = course,
                courseRepository = courseRepository
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
                    title = { Text(if(course.id == null) "Add Course Details" else "Edit Course Details") },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigateUp()
                        }) {
                            Icon(Icons.Rounded.ArrowBack, "")
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    showAddDialog.value = true
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Course to Database (Admin Only)")
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(top = 50.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    value = if (course.courseName == null) "" else course.courseName!!,
                    colors = TextFieldDefaults.textFieldColors(),
                    label = { Text("Course Name") },
                    placeholder = { Text("Enter Course Name") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    onValueChange = {
                        course.courseName = it
                    })
                Spacer(modifier = Modifier.padding(top = 20.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    value = if (course.instructor == null) "" else course.instructor!!,
                    colors = TextFieldDefaults.textFieldColors(),
                    label = { Text("Instructor") },
                    placeholder = { Text("Enter Instructor Name") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    onValueChange = {
                        course.instructor = it
                    })
                Spacer(modifier = Modifier.padding(top = 20.dp))
                GroupedCheckbox(mItemsList = listOf("MO", "TU", "WE", "TH", "FR", "SA"), course)
                Spacer(modifier = Modifier.padding(top = 20.dp))
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun EditCourseDetailsScreenPreview() {
    EditCourseDetailsScreen(rememberNavController(), Course())
}
@Composable
fun GroupedCheckbox(mItemsList: List<String>, course: Course) {
    var stringList = arrayListOf<String>()
    var length = if(course.days == null) 0 else course.days?.length!!
    while(length >= 3 && course.days != null) {
        val startIndex = length-3
        stringList.add(course.days!!.substring(startIndex, length-1))
        length-=3
    }
    val list = arrayListOf<String>()
    Row(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        mItemsList.forEach { item ->
            val isChecked = remember { mutableStateOf(item in stringList) }
            if(isChecked.value) {
                list.add(item)
            }
            Column {
                Row {
                    Checkbox(
                        checked = isChecked.value,
                        onCheckedChange = {
                            isChecked.value = it
                            if (it) {
                                list.add(item)
                            } else {
                                list.remove(item)
                            }
                            stringList = list
                            var string =
                                stringList.toString().substring(1, stringList.toString().length - 1)
                                    .filter { !it.isWhitespace() }
                            if (stringList.size > 0) {
                                string = "$string,"
                            }
                            course.days = string
                        },
                        enabled = true
                    )
                    Text(text = item)
                }
            }
        }
    }
}
@Composable
fun CourseAddConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    db: FirebaseFirestore,
    course: Course,
    courseRepository: CourseRepository
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text(if(course.id == null)"Add Course" else "Edit Course")
            },
            text = {
                Text(if(course.id == null)"Are you sure you want to add this course?" else "Are you sure you want to edit this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    if(course.id == null) {
                        courseRepository.addCourse(db, course)
                    }
                    else {
                        courseRepository.editCourse(db, course.id!!, course)
                    }
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.MainScreen.route) {
                            inclusive = true
                        }
                    }
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