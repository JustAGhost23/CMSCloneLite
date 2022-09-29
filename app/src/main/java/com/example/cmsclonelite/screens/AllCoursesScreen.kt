package com.example.cmsclonelite.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.cmsclonelite.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun AllCoursesScreen(mainNavController: NavHostController, mainViewModel: MainViewModel) {
    LaunchedEffect(Unit) {
        mainViewModel.setTitle("All Courses")
    }
    val userEnrolledCourseList: List<Course> by mainViewModel.allCoursesList.observeAsState(listOf())
    val userEnrolledCourseIdList: List<String> by mainViewModel.enrolledCourseIdList.observeAsState(listOf())
    mainViewModel.getAllCoursesList()
    mainViewModel.getCourseEnrollIdList()
    mAuth = FirebaseAuth.getInstance()
    if(userEnrolledCourseList.isEmpty()) {
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
            items(items = userEnrolledCourseList) { course ->
                AllCoursesCustomCard(
                    course = course,
                    navController = mainNavController,
                    userEnrolledCourseIdList = userEnrolledCourseIdList,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

@Composable
@Preview
fun AllCoursesScreenPreview() {
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance()
    val courseRepository = CourseRepository()
    val mainViewModel = MainViewModel(db, mAuth, courseRepository)
    AllCoursesScreen(mainNavController = rememberNavController(), mainViewModel = mainViewModel)
}
@Composable
fun AllCoursesCustomCard(course: Course, navController: NavHostController, userEnrolledCourseIdList: List<String>, mainViewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clickable(
                onClick = {
                    mainViewModel.allCoursesToCourseDetails(
                        navController,
                        course,
                        userEnrolledCourseIdList
                    )
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