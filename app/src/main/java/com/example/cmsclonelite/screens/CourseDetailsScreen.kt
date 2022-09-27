package com.example.cmsclonelite.screens

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

private lateinit var mAuth: FirebaseAuth

@Composable
fun CourseDetailsScreen(navController: NavHostController, course: Course) {
    val db = FirebaseFirestore.getInstance()
    val courseRepository = CourseRepository()
    mAuth = FirebaseAuth.getInstance()
    var userEnrolledCourseList: List<String>? by remember { mutableStateOf(mutableListOf()) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showEnrollDialog = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        userEnrolledCourseList = courseRepository.userEnrolledCourseList(db, mAuth.currentUser!!.uid)
    }
    Card {
        if (showDeleteDialog.value) {
            CourseDeletionConfirmation(showDialog = showDeleteDialog.value,
                onDismiss = {showDeleteDialog.value = false},
                navController = navController,
                db = db,
                course = course
            )
        }
        if (showEnrollDialog.value) {
            CourseEnrollConfirmation(showDialog = showEnrollDialog.value,
                onDismiss = {showEnrollDialog.value = false},
                navController = navController,
                db = db,
                course = course,
                user = mAuth.currentUser!!
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
                    title = { Text(
                        "${course.courseName}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) },
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f)
                        .padding(top =  12.dp),
                    elevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
                        ) {
                            Text(
                                "${course.courseName}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth().width(1.dp))
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
                        ) {
                            Text(
                                "Instructor: ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
                        ) {
                            Text(
                                "${course.instructor}",
                                fontSize = 16.sp
                            )
                        }
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
                        ) {
                            Text(
                                "Course Timings: ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
                        ) {
                            Text("Every ${course.days.toString()} from ${course.startDateStartTime!!.hours.toString().padStart(2, '0')}:${course.startDateStartTime!!.minutes.toString().padStart(2, '0')} to ${course.startDateEndTime!!.hours.toString().padStart(2, '0')}:${course.startDateEndTime!!.minutes.toString().padStart(2, '0')}")
                        }
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                        ) {
                            Text(
                                "Start Date: ${course.startDateStartTime!!.date.toString().padStart(2, '0')}/${(course.startDateStartTime!!.month+1).toString().padStart(2, '0')}/${course.startDateStartTime!!.year + 1900}",
                                fontSize = 16.sp
                            )
                        }
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
                        ) {
                            Text(
                                "End Date: ${course.endDateStartTime!!.date.toString().padStart(2, '0')}/${(course.endDateStartTime!!.month+1).toString().padStart(2, '0')}/${course.endDateStartTime!!.year + 1900}",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                if (mAuth.currentUser!!.uid == ADMIN_ID) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    key = "courseAnnouncements",
                                    value = course
                                )
                                navController.navigate(Screen.Announcements.route)
                            },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                        ) {
                            Icon(
                                Icons.Default.Announcement,
                                contentDescription = "Announcements (for admin only)"
                            )
                        }
                        Button(
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    key = "courseEdit",
                                    value = course
                                )
                                navController.navigate(Screen.EditCourseDetails.route)
                            },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit (for admin only)"
                            )
                        }
                        Button(
                            onClick = {
                                showDeleteDialog.value = true
                            },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete (for admin only)"
                            )
                        }
                    }
                } else if (userEnrolledCourseList != null) {
                    if (course.id.toString() !in userEnrolledCourseList!!) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    showEnrollDialog.value = true
                                },
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Enroll"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailsScreenPreview() {
    CourseDetailsScreen(rememberNavController(), Course())
}
@Composable
fun CourseDeletionConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    db: FirebaseFirestore,
    course: Course
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Delete Course")
            },
            text = {
                Text(text = "Are you sure you want to delete this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    db.collection("users").whereArrayContains("enrolled", course.id!!)
                        .get()
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                val documents = task.result
                                for(document in documents) {
                                    val user = document.id
                                    val username = document.get("name")
                                    val courseList: MutableList<String> = document.get("enrolled") as MutableList<String>
                                    courseList.remove(course.id)
                                    val userInfo = hashMapOf(
                                        "name" to username,
                                        "enrolled" to courseList
                                    )
                                    db.collection("users").document(user)
                                        .set(userInfo)
                                }
                            }
                        }
                    db.collection("courses").document("${course.id}")
                        .delete()
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e: Exception? -> Log.w(TAG, "Error deleting document", e) }
                    navController.navigate(Screen.MainScreen.route)} ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun CourseEnrollConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    db: FirebaseFirestore,
    user: FirebaseUser,
    course: Course
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Enroll in Course")
            },
            text = {
                Text(text = "Are you sure you want to enroll in this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    FirebaseMessaging.getInstance().subscribeToTopic(course.id!!)
                    db.collection("users").document("${user.uid}")
                        .update("enrolled", FieldValue.arrayUnion("${course.id}"))
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e: Exception? -> Log.w(TAG, "Error updating document", e) }
                    navController.navigate(Screen.MainScreen.route)
                } ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
