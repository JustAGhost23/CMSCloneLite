package com.example.cmsclonelite

sealed class Screen(val route: String) {
    object Login: Screen(route = "loginScreen")
    object AdminLogin: Screen(route = "adminLoginScreen")
    object MainScreen: Screen(route = "mainScreen")
    object About: Screen(route = "aboutScreen")
    object CourseDetails: Screen(route = "courseDetailsScreen")
    object EditCourseDetails: Screen(route = "editCourseDetailsScreen")
}