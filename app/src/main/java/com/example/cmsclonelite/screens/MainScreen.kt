package com.example.cmsclonelite.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.BottomBarScreen
import com.example.cmsclonelite.graphs.BottomBarNavGraph
import com.example.cmsclonelite.viewmodels.MainViewModel

@Composable
fun MainScreen(mainNavController: NavHostController, mainViewModel: MainViewModel = viewModel()) {
    val title: String by mainViewModel.screenTitle.observeAsState("")
    val bottomNavController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { title?.let { Text(it) } }
            )
        },
        bottomBar = { BottomBar(navController = bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            BottomBarNavGraph(mainNavController = mainNavController, bottomNavController = bottomNavController, mainViewModel = mainViewModel)
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.MyCourses,
        BottomBarScreen.AllCourses,
        BottomBarScreen.Profile,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    BottomNavigationItem(
        label = {
            Text(text = screen.title)
        },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = "Navigation Icon"
            )
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}