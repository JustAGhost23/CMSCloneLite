package com.example.cmsclonelite.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen

@Composable
fun EnrolledCourseDetailsScreen(navController: NavHostController, course: Course) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f)
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
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
                ) {
                    Text(
                        "Announcements",
                        textAlign = TextAlign.Start,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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