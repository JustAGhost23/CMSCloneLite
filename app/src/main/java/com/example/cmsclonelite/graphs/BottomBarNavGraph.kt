package com.example.cmsclonelite.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.cmsclonelite.BottomBarScreen
import com.example.cmsclonelite.screens.AllCoursesScreen
import com.example.cmsclonelite.screens.MyCoursesScreen
import com.example.cmsclonelite.screens.ProfileScreen
import com.example.cmsclonelite.viewmodels.MainViewModel

@Composable
fun BottomBarNavGraph(mainNavController: NavHostController, bottomNavController: NavHostController, mainViewModel: MainViewModel) {
    NavHost(
        navController = bottomNavController,
        startDestination = BottomBarScreen.MyCourses.route
    ) {
        composable(route = BottomBarScreen.MyCourses.route) {
            MyCoursesScreen(mainViewModel = mainViewModel)
        }
        composable(route = BottomBarScreen.AllCourses.route) {
            AllCoursesScreen(mainNavController = mainNavController, mainViewModel = mainViewModel)
        }
        composable(route = BottomBarScreen.Profile.route) {
            ProfileScreen(mainNavController = mainNavController, mainViewModel = mainViewModel)
        }
    }
}