package com.example.cmsclonelite.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.screens.*
import com.example.cmsclonelite.viewmodels.AnnouncementsViewModel
import com.example.cmsclonelite.viewmodels.LoginViewModel
import com.example.cmsclonelite.viewmodels.MainViewModel
import com.example.cmsclonelite.viewmodels.ProfileViewModel

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel,
    profileViewModel: ProfileViewModel,
    announcementsViewModel: AnnouncementsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController, loginViewModel = loginViewModel)
        }
        composable(route = Screen.AdminLogin.route) {
            AdminLoginScreen(navController = navController, loginViewModel = loginViewModel)
        }
        composable(route = Screen.MainScreen.route) {
            MainScreen(mainNavController = navController, mainViewModel = mainViewModel, profileViewModel = profileViewModel)
        }
        composable(route = Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(route = Screen.CourseDetails.route) {
            val course = navController.previousBackStackEntry?.savedStateHandle?.get<Course>("course")
            if (course != null) {
                CourseDetailsScreen(navController = navController, course = course)
            }
        }
        composable(route = Screen.EnrolledCourseDetails.route) {
            val course = navController.previousBackStackEntry?.savedStateHandle?.get<Course>("course")
            if (course != null) {
                EnrolledCourseDetailsScreen(navController = navController, course = course)
            }
        }
        composable(route = Screen.EditCourseDetails.route) {
            val course = navController.previousBackStackEntry?.savedStateHandle?.get<Course>("courseEdit")
            if (course != null) {
                EditCourseDetailsScreen(navController = navController, course = course)
            }
        }
        composable(route = Screen.Announcements.route) {
            val course = navController.previousBackStackEntry?.savedStateHandle?.get<Course>("courseAnnouncements")
            if (course != null) {
                AnnouncementsScreen(navController = navController, course = course, announcementsViewModel = announcementsViewModel)
            }
        }
        composable(route = Screen.AddAnnouncements.route) {
            val course = navController.previousBackStackEntry?.savedStateHandle?.get<Course>("courseAnnouncements")
            if (course != null) {
                AddAnnouncementsScreen(navController = navController, course = course, announcementsViewModel = announcementsViewModel)
            }
        }
    }
}