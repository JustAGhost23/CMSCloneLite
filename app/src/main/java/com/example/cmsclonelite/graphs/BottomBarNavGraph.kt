package com.example.cmsclonelite.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.cmsclonelite.BottomBarScreen
import com.example.cmsclonelite.screens.AllCoursesScreen
import com.example.cmsclonelite.screens.MyCoursesScreen
import com.example.cmsclonelite.screens.SettingsScreen

@Composable
fun BottomBarNavGraph(mainNavController: NavHostController, bottomNavController: NavHostController) {
    NavHost(
        navController = bottomNavController,
        startDestination = BottomBarScreen.MyCourses.route
    ) {
        composable(route = BottomBarScreen.MyCourses.route) {
            MyCoursesScreen()
        }
        composable(route = BottomBarScreen.AllCourses.route) {
            AllCoursesScreen()
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen(mainNavController = mainNavController)
        }
    }
}