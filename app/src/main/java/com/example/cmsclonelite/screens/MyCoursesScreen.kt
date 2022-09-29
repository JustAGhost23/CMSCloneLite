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

const val ADMIN_ID = "HT8sVmAC1tSwkoOVcscEphEWYjS2"
private lateinit var mAuth: FirebaseAuth

@Composable
fun MyCoursesScreen(mainNavController: NavHostController, mainViewModel: MainViewModel) {
    LaunchedEffect(Unit) {
        mainViewModel.setTitle("My Courses")
    }
    val enrolledCourseList: List<Course> by mainViewModel.enrolledCourseList.observeAsState(listOf())
    mainViewModel.getCoursesEnrolledList()
    mAuth = FirebaseAuth.getInstance()

    if (enrolledCourseList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if(mAuth.currentUser!!.uid == ADMIN_ID) "Admin" else "Not enrolled in any course yet",
                fontSize = MaterialTheme.typography.h5.fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 20.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = enrolledCourseList) { course ->
                MyCoursesCustomCard(
                    course = course,
                    navController = mainNavController,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

@Composable
@Preview
fun MyCoursesScreenPreview() {
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance()
    val courseRepository = CourseRepository()
    val mainViewModel = MainViewModel(db, mAuth, courseRepository)
    MyCoursesScreen(mainNavController = rememberNavController(), mainViewModel = mainViewModel)
}
@Composable
fun MyCoursesCustomCard(course: Course, navController: NavHostController, mainViewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clickable(
                onClick = {
                    mainViewModel.myCoursesToEnrolledCourse(navController, course)
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