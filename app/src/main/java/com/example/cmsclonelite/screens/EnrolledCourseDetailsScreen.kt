package com.example.cmsclonelite.screens

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.CourseDetailsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun EnrolledCourseDetailsScreen(
    navController: NavHostController,
    course: Course,
    courseDetailsViewModel: CourseDetailsViewModel
) {
    mAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val showCalendarDialog: Boolean by courseDetailsViewModel.isCalendarDialog.observeAsState(false)
    val showUnenrollDialog: Boolean by courseDetailsViewModel.isUnenrollDialog.observeAsState(false)
    courseDetailsViewModel.initialize()
    val requestWritePermissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            courseDetailsViewModel.calendarExport(context, navController, course)
        }
    }
    val requestReadPermissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            Toast.makeText(context, "Permission provided", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(context, "Permission rejected", Toast.LENGTH_SHORT).show()
        }
    }
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
        Card {
            if (showCalendarDialog) {
                CalendarExportConfirmation(
                    showDialog = showCalendarDialog,
                    onDismiss = {courseDetailsViewModel.removeCalendarDialog()},
                    navController = navController,
                    course = course,
                    context = context,
                    courseDetailsViewModel = courseDetailsViewModel,
                    requestWritePermissionsLauncher = requestWritePermissionsLauncher,
                    requestReadPermissionsLauncher =  requestReadPermissionsLauncher
                )
            }
            if(showUnenrollDialog) {
                CourseUnenrollConfirmation(
                    showDialog = showUnenrollDialog,
                    onDismiss = {courseDetailsViewModel.removeUnenrollDialog()},
                    navController = navController,
                    course = course,
                    courseDetailsViewModel = courseDetailsViewModel
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
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
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 12.dp)
                    .clickable(onClick = {
                        courseDetailsViewModel.courseDetailsToAnnouncements(navController, course)
                    }),
                elevation = 24.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
                ) {
                    Text(
                        "Announcements",
                        textAlign = TextAlign.Start,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.fillMaxWidth(0.85f))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Announcements Arrow")
                }
            }
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
                        courseDetailsViewModel.showCalendarDialog()
                    },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Calendar export")
                }
                Button(
                    onClick = {
                        courseDetailsViewModel.showUnenrollDialog()
                    },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Unenroll"
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun EnrolledCourseDetailsScreenPreview() {
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance()
    val courseRepository = CourseRepository()
    val courseDetailsViewModel = CourseDetailsViewModel(db, mAuth, courseRepository)
    EnrolledCourseDetailsScreen(rememberNavController(), Course(), courseDetailsViewModel)
}
@Composable
fun CourseUnenrollConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    course: Course,
    courseDetailsViewModel: CourseDetailsViewModel
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Unenroll from Course")
            },
            text = {
                Text(text = "Are you sure you want to unenroll from this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    courseDetailsViewModel.unenrollFromCourse(navController, course)
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
fun CalendarExportConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    course: Course,
    context: Context,
    courseDetailsViewModel: CourseDetailsViewModel,
    requestWritePermissionsLauncher: ManagedActivityResultLauncher<String, Boolean>,
    requestReadPermissionsLauncher: ManagedActivityResultLauncher<String, Boolean>
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Add to Calendar")
            },
            text = {
                Text(text = "Do you want to add class reminders to your Calendar?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    if(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PERMISSION_GRANTED) {
                        requestReadPermissionsLauncher.launch(Manifest.permission.READ_CALENDAR)
                        requestWritePermissionsLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                    }
                    else if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PERMISSION_GRANTED) {
                            requestReadPermissionsLauncher.launch(Manifest.permission.READ_CALENDAR)
                        }
                    else if(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PERMISSION_GRANTED) {
                            requestWritePermissionsLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                        }
                    else {
                        courseDetailsViewModel.calendarExport(context, navController, course)
                    }
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