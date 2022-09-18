package com.example.cmsclonelite.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.screens.AdminLoginScreen
import com.example.cmsclonelite.screens.LoginScreen
import com.example.cmsclonelite.screens.MainScreen

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
    }
}