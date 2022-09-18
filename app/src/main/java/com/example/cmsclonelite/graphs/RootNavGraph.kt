package com.example.cmsclonelite.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.screens.*

@Composable
fun SetupNavGraph(
    navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(route = Screen.AdminLogin.route) {
            AdminLoginScreen(navController = navController)
        }
        composable(route = Screen.MainScreen.route) {
            MainScreen(mainNavController = navController)
        }
        composable(route = Screen.About.route) {
            AboutScreen()
        }
    }
}