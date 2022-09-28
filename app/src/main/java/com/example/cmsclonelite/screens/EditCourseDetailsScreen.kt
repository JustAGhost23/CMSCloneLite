package com.example.cmsclonelite.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun EditCourseDetailsScreen(navController: NavHostController, course: Course) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val courseRepository = CourseRepository()
    val mCalendar = Calendar.getInstance()
    val mStartYear = if(course.id == null)mCalendar.get(Calendar.YEAR) else course.startDateStartTime!!.year + 1900
    val mStartMonth = if(course.id == null)mCalendar.get(Calendar.MONTH) else course.startDateStartTime!!.month
    val mStartDay = if(course.id == null)mCalendar.get(Calendar.DAY_OF_MONTH) else course.startDateStartTime!!.date
    val mEndYear = if(course.id == null)mCalendar.get(Calendar.YEAR) else course.endDateStartTime!!.year + 1900
    val mEndMonth = if(course.id == null)mCalendar.get(Calendar.MONTH) else course.endDateStartTime!!.month
    val mEndDay = if(course.id == null)mCalendar.get(Calendar.DAY_OF_MONTH) else course.endDateStartTime!!.date
    val mStartHour = if(course.id == null)mCalendar[Calendar.HOUR_OF_DAY] else course.startDateStartTime!!.hours
    val mStartMinute = if(course.id == null)mCalendar[Calendar.MINUTE] else course.startDateStartTime!!.minutes
    val mEndHour = if(course.id == null)mCalendar[Calendar.HOUR_OF_DAY] else course.startDateEndTime!!.hours
    val mEndMinute = if(course.id == null)mCalendar[Calendar.MINUTE] else course.startDateEndTime!!.minutes
    var courseName by rememberSaveable { mutableStateOf(if(course.id == null)"" else course.courseName)}
    var instructor by rememberSaveable { mutableStateOf(if(course.id == null)"" else course.instructor)}
    val showAddDialog = remember { mutableStateOf(false) }
    val mEndTimePickerDialog = TimePickerDialog(
        context,
        {_, mHour : Int, mMinute: Int ->
            if(course.startDateStartTime == null) course.startDateStartTime = Date()
            if(course.startDateEndTime == null) course.startDateEndTime = Date()
            if(course.endDateStartTime == null) course.endDateStartTime = Date()
            if(course.endDateEndTime == null) course.endDateEndTime = Date()
            course.startDateEndTime!!.hours = mHour
            course.startDateEndTime!!.minutes = mMinute
            course.startDateEndTime!!.seconds = 0
            course.endDateEndTime!!.hours = mHour
            course.endDateEndTime!!.minutes = mMinute
            course.endDateEndTime!!.seconds = 0
        }, mEndHour, mEndMinute, false
    )
    val mStartTimePickerDialog = TimePickerDialog(
        context,
        {_, mHour : Int, mMinute: Int ->
            if(course.startDateStartTime == null) course.startDateStartTime = Date()
            if(course.startDateEndTime == null) course.startDateEndTime = Date()
            if(course.endDateStartTime == null) course.endDateStartTime = Date()
            if(course.endDateEndTime == null) course.endDateEndTime = Date()
            course.startDateStartTime!!.hours = mHour
            course.startDateStartTime!!.minutes = mMinute
            course.startDateStartTime!!.seconds = 0
            course.endDateStartTime!!.hours = mHour
            course.endDateStartTime!!.minutes = mMinute
            course.endDateStartTime!!.seconds = 0
            mEndTimePickerDialog.show()
        }, mStartHour, mStartMinute, false
    )
    val mEndDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            if(course.startDateStartTime == null) course.startDateStartTime = Date()
            if(course.startDateEndTime == null) course.startDateEndTime = Date()
            if(course.endDateStartTime == null) course.endDateStartTime = Date()
            if(course.endDateEndTime == null) course.endDateEndTime = Date()
            course.endDateStartTime!!.year = mYear - 1900
            course.endDateStartTime!!.month = mMonth
            course.endDateStartTime!!.date = mDayOfMonth
            course.endDateEndTime!!.year = mYear - 1900
            course.endDateEndTime!!.month = mMonth
            course.endDateEndTime!!.date = mDayOfMonth
            mStartTimePickerDialog.show()
        }, mEndYear, mEndMonth, mEndDay
    )
    val mStartDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            if(course.startDateStartTime == null) course.startDateStartTime = Date()
            if(course.startDateEndTime == null) course.startDateEndTime = Date()
            if(course.endDateStartTime == null) course.endDateStartTime = Date()
            if(course.endDateEndTime == null) course.endDateEndTime = Date()
            course.startDateStartTime!!.year = mYear - 1900
            course.startDateStartTime!!.month = mMonth
            course.startDateStartTime!!.date = mDayOfMonth
            course.startDateEndTime!!.year = mYear - 1900
            course.startDateEndTime!!.month = mMonth
            course.startDateEndTime!!.date = mDayOfMonth
            mEndDatePickerDialog.show()
        }, mStartYear, mStartMonth, mStartDay
    )
    Card {
        if (showAddDialog.value) {
            CourseAddConfirmation(showDialog = showAddDialog.value,
                onDismiss = {showAddDialog.value = false},
                navController = navController,
                db = db,
                course = course,
                context = context,
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
                    if(course.id == null) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add Course to Database (Admin Only)"
                        )
                    }
                    else {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit Course to Database (Admin Only)"
                        )
                    }
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
                    value = courseName!!,
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
                        courseName = it
                        course.courseName = courseName
                    })
                Spacer(modifier = Modifier.padding(top = 20.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    value = instructor!!,
                    colors = TextFieldDefaults.textFieldColors(),
                    label = { Text("Instructor") },
                    placeholder = { Text("Enter Instructor Name") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    onValueChange = {
                        instructor = it
                        course.instructor = instructor
                    })
                Spacer(modifier = Modifier.padding(top = 20.dp))
                GroupedCheckbox(mItemsList = listOf("MO", "TU", "WE", "TH", "FR", "SA"), course)
                Spacer(modifier = Modifier.padding(top = 40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            mStartDatePickerDialog.show()
                        }
                    ) {
                        Text(text = if(course.id == null)"Enter Course Timings" else "Edit Course Timings",
                            fontSize = 16.sp)
                    }
                }
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
    context: Context,
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
                        if(course.startDateStartTime != null && course.startDateEndTime != null && course.endDateStartTime != null && course.endDateEndTime != null && course.days != "") {
                            courseRepository.addCourse(db, course)
                            navController.navigate(Screen.MainScreen.route) {
                                popUpTo(Screen.MainScreen.route) {
                                    inclusive = true
                                }
                            }
                        }
                        else {
                            Toast.makeText(context.findActivity(), "Please enter all details",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        if(course.startDateStartTime != null && course.startDateEndTime != null && course.endDateStartTime != null && course.endDateEndTime != null && course.days != "") {
                            courseRepository.editCourse(db, course.id!!, course)
                            navController.navigate(Screen.MainScreen.route) {
                                popUpTo(Screen.MainScreen.route) {
                                    inclusive = true
                                }
                            }
                        }
                        else {
                            Toast.makeText(context.findActivity(), "Please enter all details",
                                Toast.LENGTH_SHORT).show()
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