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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.*

private lateinit var mAuth: FirebaseAuth

@Composable
fun EnrolledCourseDetailsScreen(navController: NavHostController, course: Course) {
    mAuth = FirebaseAuth.getInstance()
    val email = mAuth.currentUser!!.email
    val context = LocalContext.current
    val showCalendarDialog = remember { mutableStateOf(false) }
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
        }
        AddFAB(FabPosition.Center, onClick = {
            showCalendarDialog.value = true
        }) {
            Icon(Icons.Filled.CalendarToday, contentDescription = "Calendar export")
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                    //TODO: Make it stay apart
                    Spacer(modifier = Modifier.padding(horizontal =80.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Announcements Arrow")
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