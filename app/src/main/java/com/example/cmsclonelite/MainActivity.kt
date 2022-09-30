package com.example.cmsclonelite

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.graphs.SetupNavGraph
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.ui.theme.CMSCloneLiteTheme
import com.example.cmsclonelite.viewmodels.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val courseRepository = CourseRepository()

        val loginViewModel: LoginViewModel by viewModels {
            LoginViewModelFactory(db, mAuth)
        }

        val mainViewModel: MainViewModel by viewModels {
            MainViewModelFactory(db, mAuth, courseRepository)
        }

        val profileViewModel: ProfileViewModel by viewModels {
            ProfileViewModelFactory()
        }

        val announcementsViewModel: AnnouncementsViewModel by viewModels {
            AnnouncementsViewModelFactory(db, courseRepository)
        }

        val courseDetailsViewModel: CourseDetailsViewModel by viewModels {
            CourseDetailsViewModelFactory(db, mAuth, courseRepository)
        }

        val editCourseDetailsViewModel: EditCourseDetailsViewModel by viewModels {
            EditCourseDetailsViewModelFactory(db, courseRepository)
        }

        createNotificationChannel()

        val sharedPrefs: SharedPreferences = getSharedPreferences("PREFERENCES", MODE_PRIVATE)
        val dark = sharedPrefs.getBoolean("darkTheme", false)
        setContent {
            val darkTheme: Boolean by profileViewModel.isDarkTheme.observeAsState(dark)
            CMSCloneLiteTheme(darkTheme = darkTheme){
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                navController = rememberNavController()
                SetupNavGraph(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    mainViewModel = mainViewModel,
                    profileViewModel = profileViewModel,
                    announcementsViewModel = announcementsViewModel,
                    courseDetailsViewModel = courseDetailsViewModel,
                    editCourseDetailsViewModel = editCourseDetailsViewModel
                )
                if(mAuth.currentUser == null) {
                    navController.navigate(route = Screen.Login.route) {
                        popUpTo(Screen.MainScreen.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Announcements"
            val description = "Announcement Push Notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("Announcements", name, importance)
            channel.description = description
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}