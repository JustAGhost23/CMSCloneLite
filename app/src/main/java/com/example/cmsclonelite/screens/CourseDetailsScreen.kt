package com.example.cmsclonelite.screens

import android.content.Context
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.CourseDetailsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun CourseDetailsScreen(
    navController: NavHostController,
    course: Course,
    courseDetailsViewModel: CourseDetailsViewModel
) {
    mAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val showDeleteDialog: Boolean by courseDetailsViewModel.isDeleteDialog.observeAsState(false)
    val showEnrollDialog: Boolean by courseDetailsViewModel.isEnrollDialog.observeAsState(false)
    val userEnrolledCourseList: List<String> by courseDetailsViewModel.userEnrolledCourseList.observeAsState(listOf())
    courseDetailsViewModel.getUserEnrolledCourseList()
    courseDetailsViewModel.initialize()
    Card {
        if (showDeleteDialog) {
            CourseDeletionConfirmation(
                showDialog = showDeleteDialog,
                onDismiss = {courseDetailsViewModel.removeDeleteDialog()},
                navController = navController,
                course = course,
                courseDetailsViewModel = courseDetailsViewModel
            )
        }
        if (showEnrollDialog) {
            CourseEnrollConfirmation(
                showDialog = showEnrollDialog,
                onDismiss = {courseDetailsViewModel.removeEnrollDialog()},
                navController = navController,
                course = course,
                courseDetailsViewModel = courseDetailsViewModel
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
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 12.dp),
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
                        Divider(color = Color.LightGray, modifier = Modifier
                            .fillMaxWidth()
                            .width(1.dp))
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
                            Text("Every ${course.days.toString()} from ${course.startDateStartTime!!.hours.toString().padStart(2, '0')}:${course.startDateStartTime!!.minutes.toString().padStart(2, '0')} to ${course.endDateEndTime!!.hours.toString().padStart(2, '0')}:${course.endDateEndTime!!.minutes.toString().padStart(2, '0')}")
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
                                "End Date: ${course.endDateEndTime!!.date.toString().padStart(2, '0')}/${(course.endDateEndTime!!.month+1).toString().padStart(2, '0')}/${course.endDateEndTime!!.year + 1900}",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                if (mAuth.currentUser!!.uid == ADMIN_ID) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                courseDetailsViewModel.courseDetailsToAnnouncements(navController, course)
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
                                courseDetailsViewModel.courseDetailsToEditCourse(navController, course)
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
                                courseDetailsViewModel.showDeleteDialog()
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
                } else {
                    if (course.id.toString() !in userEnrolledCourseList) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    courseDetailsViewModel.showEnrollDialog()
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
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance()
    val courseRepository = CourseRepository()
    val courseDetailsViewModel = CourseDetailsViewModel(db, mAuth, courseRepository)
    CourseDetailsScreen(rememberNavController(), Course(), courseDetailsViewModel)
}
@Composable
fun CourseDeletionConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    course: Course,
    courseDetailsViewModel: CourseDetailsViewModel
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
                    courseDetailsViewModel.deleteCourse(navController, course)
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
@Composable
fun CourseEnrollConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    course: Course,
    courseDetailsViewModel: CourseDetailsViewModel
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
                    courseDetailsViewModel.enrollInCourse(navController, course)
                }) {
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
