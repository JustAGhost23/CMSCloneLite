package com.example.cmsclonelite.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.EditCourseDetailsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun EditCourseDetailsScreen(
    navController: NavHostController,
    course: Course,
    editCourseDetailsViewModel: EditCourseDetailsViewModel
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val courseName: String by editCourseDetailsViewModel.courseName.observeAsState("")
    val instructor: String by editCourseDetailsViewModel.instructor.observeAsState("")
    val showAddDialog: Boolean by editCourseDetailsViewModel.isAddCourseDialog.observeAsState(false)
    val showEditDialog: Boolean by editCourseDetailsViewModel.isEditCourseDialog.observeAsState(false)
    val startDateStartTime: Date by editCourseDetailsViewModel.startDateStartTime.observeAsState(Date())
    val endDateEndTime: Date by editCourseDetailsViewModel.endDateEndTime.observeAsState(Date())
    editCourseDetailsViewModel.initialize(course)
    val mEndTimePickerDialog = TimePickerDialog(
        context,
        android.R.style.Theme_DeviceDefault_Dialog,
        {_, mHour : Int, mMinute: Int ->
            endDateEndTime.hours = mHour
            endDateEndTime.minutes = mMinute
            endDateEndTime.seconds = 0
            editCourseDetailsViewModel.addEndDateEndTime(endDateEndTime, course)
        }, endDateEndTime.hours, endDateEndTime.minutes, false
    )
    val mStartTimePickerDialog = TimePickerDialog(
        context,
        android.R.style.Theme_DeviceDefault_Dialog,
        {_, mHour : Int, mMinute: Int ->
            startDateStartTime.hours = mHour
            startDateStartTime.minutes = mMinute
            startDateStartTime.seconds = 0
            editCourseDetailsViewModel.addStartDateStartTime(startDateStartTime, course)
            mEndTimePickerDialog.setMessage("Set Class End Time")
            mEndTimePickerDialog.show()
        }, startDateStartTime.hours, startDateStartTime.minutes, false
    )
    val mEndDatePickerDialog = DatePickerDialog(
        context,
        android.R.style.Theme_DeviceDefault_Dialog,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            endDateEndTime.year = mYear - 1900
            endDateEndTime.month = mMonth
            endDateEndTime.date = mDayOfMonth
            editCourseDetailsViewModel.addEndDateEndTime(endDateEndTime, course)
            mStartTimePickerDialog.setMessage("Set Class Start Time")
            mStartTimePickerDialog.show()
        }, endDateEndTime.year + 1900, endDateEndTime.month, endDateEndTime.date
    )
    val mStartDatePickerDialog = DatePickerDialog(
        context,
        android.R.style.Theme_DeviceDefault_Dialog,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            startDateStartTime.year = mYear - 1900
            startDateStartTime.month = mMonth
            startDateStartTime.date = mDayOfMonth
            editCourseDetailsViewModel.addStartDateStartTime(startDateStartTime, course)
            mEndDatePickerDialog.setMessage("Set Class End Date")
            mEndDatePickerDialog.show()
        }, startDateStartTime.year + 1900, startDateStartTime.month, startDateStartTime.date
    )
    Card {
        if (showAddDialog) {
            CourseAddConfirmation(
                showDialog = showAddDialog,
                onDismiss = {editCourseDetailsViewModel.removeAddCourseDialog()},
                navController = navController,
                course = course,
                context = context,
                editCourseDetailsViewModel = editCourseDetailsViewModel
            )
        }
        if (showEditDialog) {
            CourseEditConfirmation(
                showDialog = showEditDialog,
                onDismiss = {editCourseDetailsViewModel.removeEditCourseDialog()},
                navController = navController,
                course = course,
                context = context,
                editCourseDetailsViewModel = editCourseDetailsViewModel
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
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    if(course.id == null) {
                        editCourseDetailsViewModel.showAddCourseDialog()
                    }
                    else {
                        editCourseDetailsViewModel.showEditCourseDialog()
                    }
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
                    value = courseName,
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
                        editCourseDetailsViewModel.setCourseName(course, it)
                    })
                Spacer(modifier = Modifier.padding(top = 20.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    value = instructor,
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
                        editCourseDetailsViewModel.setInstructor(course, it)
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
                            mStartDatePickerDialog.setMessage("Set Class Start Date")
                            mStartDatePickerDialog.show()
                        }
                    ) {
                        Text("Edit Course Timings",
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
    val db = FirebaseFirestore.getInstance()
    val courseRepository = CourseRepository()
    val editCourseDetailsViewModel = EditCourseDetailsViewModel(db, courseRepository)
    EditCourseDetailsScreen(rememberNavController(), Course(), editCourseDetailsViewModel)
}
@Composable
fun GroupedCheckbox(mItemsList: List<String>, course: Course) {
    var stringList = arrayListOf<String>()
    var length = if(course.days == null) 0 else course.days?.length!!
    val list = arrayListOf<String>()
    while(length >= 3 && course.days != null) {
        val startIndex = length-3
        stringList.add(course.days!!.substring(startIndex, length-1))
        length-=3
    }
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
    course: Course,
    context: Context,
    editCourseDetailsViewModel: EditCourseDetailsViewModel
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Add Course")
            },
            text = {
                Text("Are you sure you want to add this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    editCourseDetailsViewModel.postCourseToDatabase(context, navController, course)
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
fun CourseEditConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    course: Course,
    context: Context,
    editCourseDetailsViewModel: EditCourseDetailsViewModel
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Edit Course")
            },
            text = {
                Text("Are you sure you want to edit this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    editCourseDetailsViewModel.postCourseToDatabase(context, navController, course)
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