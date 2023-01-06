package com.example.cmsclonelite

sealed class Screen(val route: String) {
    object Login: Screen(route = "loginScreen")
    object AdminLogin: Screen(route = "adminLoginScreen")
    object MainScreen: Screen(route = "mainScreen")
    object About: Screen(route = "aboutScreen")
    object CourseDetails: Screen(route = "courseDetailsScreen")
    object EnrolledCourseDetails: Screen(route = "enrolledCourseDetailsScreen")
    object EditCourseDetails: Screen(route = "editCourseDetailsScreen")
    object Announcements: Screen(route = "announcementsScreen")
    object AddAnnouncements: Screen(route = "addAnnouncementsScreen")
    object DetailedAnnouncement: Screen(route = "detailedAnnouncement")
}