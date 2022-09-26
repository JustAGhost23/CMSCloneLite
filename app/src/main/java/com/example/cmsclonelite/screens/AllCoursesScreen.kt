package com.example.cmsclonelite.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun AllCoursesScreen(mainNavController: NavHostController, mainViewModel: MainViewModel) {
    var list: List<Course> by remember { mutableStateOf(mutableListOf()) }
    var userEnrolledCourseList: List<String>? by remember { mutableStateOf(mutableListOf()) }
    mAuth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val courseRepository = CourseRepository()
    LaunchedEffect(Unit) {
        mainViewModel.setTitle("All Courses")
        userEnrolledCourseList = courseRepository.userEnrolledCourseList(db, mAuth.currentUser!!.uid)
        list = courseRepository.getData(db = db)
    }
    if(list.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No courses added yet",
                fontSize = MaterialTheme.typography.h5.fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
    else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 20.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = list) { course ->
                AllCoursesCustomCard(course = course, navController = mainNavController, userEnrolledCourseList = userEnrolledCourseList)
            }
        }
    }
}

@Composable
@Preview
fun AllCoursesScreenPreview() {
    val mainViewModel = MainViewModel()
    AllCoursesScreen(mainNavController = rememberNavController(), mainViewModel = mainViewModel)
}
@Composable
fun AllCoursesCustomCard(course: Course, navController: NavHostController, userEnrolledCourseList: List<String>?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clickable(
                onClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        key = "course",
                        value = course
                    )
                    if (userEnrolledCourseList != null) {
                        if(course.id.toString() in userEnrolledCourseList) {
                            navController.navigate(Screen.EnrolledCourseDetails.route)
                        }
                        else {
                            navController.navigate(Screen.CourseDetails.route)
                        }
                    }
                    else {
                        navController.navigate(Screen.CourseDetails.route)
                    }
                }
            ),
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
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    "${course.instructor}",
                    fontSize = 16.sp
                )
            }
        }
    }
}