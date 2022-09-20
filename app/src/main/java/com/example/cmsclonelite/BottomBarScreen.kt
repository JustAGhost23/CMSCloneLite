package com.example.cmsclonelite

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object MyCourses : BottomBarScreen(
        route = "myCourses",
        title = "My Courses",
        icon = Icons.Default.List
    )

    object AllCourses : BottomBarScreen(
        route = "allCourses",
        title = "All Courses",
        icon = Icons.Default.ListAlt
    )

    object Profile : BottomBarScreen(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
}