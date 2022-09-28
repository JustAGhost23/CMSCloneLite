package com.example.cmsclonelite.screens

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.cmsclonelite.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

private lateinit var mAuth: FirebaseAuth

@Composable
fun EnrolledCourseDetailsScreen(navController: NavHostController, course: Course) {
    mAuth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val email = mAuth.currentUser!!.email
    val context = LocalContext.current
    val showCalendarDialog = remember { mutableStateOf(false) }
    val showUnenrollDialog = remember { mutableStateOf(false) }
    val requestWritePermissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            showCalendarDialog.value = false
            val calID: Long? = getCalendarId(context, email!!)
            val startMillis: Long = Calendar.getInstance().run {
                set(
                    course.startDateStartTime!!.year + 1900,
                    course.startDateStartTime!!.month,
                    course.startDateStartTime!!.date,
                    course.startDateStartTime!!.hours,
                    course.startDateStartTime!!.minutes
                )
                timeInMillis
            }
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.TITLE, course.courseName)
                put(CalendarContract.Events.CALENDAR_ID, calID)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(
                    CalendarContract.Events.DURATION,
                    "PT${course.endDateEndTime!!.hours - course.startDateStartTime!!.hours}H${course.endDateEndTime!!.minutes - course.startDateStartTime!!.minutes}M"
                )
                put(
                    CalendarContract.Events.RRULE,
                    "FREQ=WEEKLY;UNTIL=${course.endDateStartTime!!.year + 1900}${
                        (course.endDateStartTime!!.month + 1).toString()
                            .padStart(2, '0')
                    }${
                        course.endDateStartTime!!.date.toString().padStart(2, '0')
                    }T${
                        course.endDateEndTime!!.hours.toString().padStart(2, '0')
                    }${
                        course.endDateEndTime!!.minutes.toString().padStart(2, '0')
                    }${
                        course.endDateEndTime!!.seconds.toString().padStart(2, '0')
                    }Z;BYDAY=${course.days.toString().substring(0, (course.days.toString().length-1))}"
                )
            }
            context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI,
                values
            )!!
            Toast.makeText(context, "Calendar Events added", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(Screen.MainScreen.route) {
                    inclusive = true
                }
            }
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
    ) {
        Card {
            if (showCalendarDialog.value) {
                CalendarExportConfirmation(showDialog = showCalendarDialog.value,
                    onDismiss = {showCalendarDialog.value = false},
                    navController = navController,
                    course = course,
                    email = email!!,
                    context = context,
                    requestWritePermissionsLauncher = requestWritePermissionsLauncher,
                    requestReadPermissionsLauncher =  requestReadPermissionsLauncher
                )
            }
            if(showUnenrollDialog.value) {
                CourseUnenrollConfirmation(
                    showDialog = showUnenrollDialog.value,
                    onDismiss = {showUnenrollDialog.value = false},
                    navController = navController,
                    db = db,
                    user = mAuth.currentUser!!,
                    course = course
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
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 12.dp)
                    .clickable(onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            key = "courseAnnouncements",
                            value = course
                        )
                        navController.navigate(Screen.Announcements.route)
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
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        showCalendarDialog.value = true
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
                        showUnenrollDialog.value = true
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
    EnrolledCourseDetailsScreen(navController = rememberNavController(), course = Course())
}
@Composable
fun CourseUnenrollConfirmation(
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
                Text("Unenroll from Course")
            },
            text = {
                Text(text = "Are you sure you want to unenroll from this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(course.id!!)
                    db.collection("users").document("${user.uid}")
                        .update("enrolled", FieldValue.arrayRemove("${course.id}"))
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
@Composable
fun CalendarExportConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    course: Course,
    email: String,
    context: Context,
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
                        val calID: Long? = getCalendarId(context, email)
                        val startMillis: Long = Calendar.getInstance().run {
                            set(
                                course.startDateStartTime!!.year + 1900,
                                course.startDateStartTime!!.month,
                                course.startDateStartTime!!.date,
                                course.startDateStartTime!!.hours,
                                course.startDateStartTime!!.minutes
                            )
                            timeInMillis
                        }
                        val values = ContentValues().apply {
                            put(CalendarContract.Events.DTSTART, startMillis)
                            put(CalendarContract.Events.TITLE, course.courseName)
                            put(CalendarContract.Events.CALENDAR_ID, calID)
                            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                            put(
                                CalendarContract.Events.DURATION,
                                "PT${course.endDateEndTime!!.hours - course.startDateStartTime!!.hours}H${course.endDateEndTime!!.minutes - course.startDateStartTime!!.minutes}M"
                            )
                            put(
                                CalendarContract.Events.RRULE,
                                "FREQ=WEEKLY;UNTIL=${course.endDateStartTime!!.year + 1900}${
                                    (course.endDateStartTime!!.month + 1).toString()
                                        .padStart(2, '0')
                                }${
                                    course.endDateStartTime!!.date.toString().padStart(2, '0')
                                }T${
                                    course.endDateEndTime!!.hours.toString().padStart(2, '0')
                                }${
                                    course.endDateEndTime!!.minutes.toString().padStart(2, '0')
                                }${
                                    course.endDateEndTime!!.seconds.toString().padStart(2, '0')
                                }Z;BYDAY=${course.days.toString().substring(0, (course.days.toString().length-1))}"
                            );
                        }
                        context.contentResolver.insert(
                            CalendarContract.Events.CONTENT_URI,
                            values
                        )!!
                        Toast.makeText(context, "Calendar Events added", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.MainScreen.route) {
                                inclusive = true
                            }
                        }
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
private fun getCalendarId(context: Context, email: String) : Long? {
    val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

    var calCursor = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.ACCOUNT_NAME + " = '$email'",
        null,
        CalendarContract.Calendars._ID + " ASC"
    )

    if (calCursor != null && calCursor.count <= 0) {
        calCursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.ACCOUNT_NAME + " = '$email'",
            null,
            CalendarContract.Calendars._ID + " ASC"
        )
    }

    if (calCursor != null) {
        if (calCursor.moveToFirst()) {
            val calName: String
            val calID: String
            val nameCol = calCursor.getColumnIndex(projection[1])
            val idCol = calCursor.getColumnIndex(projection[0])

            calName = calCursor.getString(nameCol)
            calID = calCursor.getString(idCol)

            Log.d(TAG, "Calendar name = $calName Calendar ID = $calID")

            calCursor.close()
            return calID.toLong()
        }
    }
    return null
}