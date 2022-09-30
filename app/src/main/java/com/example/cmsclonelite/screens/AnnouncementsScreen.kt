package com.example.cmsclonelite.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ArrowBack
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
import com.example.cmsclonelite.Announcement
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.AnnouncementsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun AnnouncementsScreen(
    navController: NavHostController,
    course: Course,
    announcementsViewModel: AnnouncementsViewModel
) {
    mAuth = FirebaseAuth.getInstance()
    val announcementList: List<Announcement> by announcementsViewModel.allAnnouncementsList.observeAsState(listOf())
    announcementsViewModel.getAllAnnouncementsList(course.id!!)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "Announcements"
                ) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(Icons.Rounded.ArrowBack, "")
                    }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if(mAuth.currentUser!!.uid == ADMIN_ID) {
                FloatingActionButton(onClick = {
                    announcementsViewModel.allAnnouncementsToAddCourseDetails(navController, course)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Courses (Admin Only)")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 20.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = announcementList) { announcement ->
                    CustomAnnouncementCard(announcement = announcement)
                }
            }
        }
    }
}

@Composable
@Preview
fun AnnouncementsScreenPreview() {
    val db = FirebaseFirestore.getInstance()
    val courseRepository = CourseRepository()
    val announcementsViewModel = AnnouncementsViewModel(db, courseRepository)
    AnnouncementsScreen(rememberNavController(), Course(), announcementsViewModel)
}
@Composable
fun CustomAnnouncementCard(announcement: Announcement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        elevation = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
            ) {
                Text(
                    "${announcement.title}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    "${announcement.body}",
                    fontSize = 16.sp
                )
            }
        }
    }
}