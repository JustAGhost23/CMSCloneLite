package com.example.cmsclonelite.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.BottomBarScreen
import com.example.cmsclonelite.graphs.BottomBarNavGraph
import com.example.cmsclonelite.viewmodels.MainViewModel
import com.example.cmsclonelite.viewmodels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

private lateinit var mAuth: FirebaseAuth

@Composable
fun MainScreen(mainNavController: NavHostController, mainViewModel: MainViewModel, profileViewModel: ProfileViewModel) {
    mAuth = FirebaseAuth.getInstance()
    val title: String by mainViewModel.screenTitle.observeAsState("")
    val showUnenrollAllDialog: Boolean by mainViewModel.isUnenrollAllDialog.observeAsState(false)
    val userEnrolledCourseList: List<String> by mainViewModel.enrolledCourseIdList.observeAsState(listOf())
    mainViewModel.getCourseEnrollIdList()
    val bottomNavController = rememberNavController()
    Card {
        if (showUnenrollAllDialog) {
            CourseUnenrollAllConfirmation(
                showDialog = showUnenrollAllDialog,
                onDismiss = { mainViewModel.removeUnenrollDialog() },
                navController = mainNavController,
                userEnrolledCourseList = userEnrolledCourseList,
                mainViewModel = mainViewModel
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) }
            )
        },
        bottomBar = { BottomBar(navController = bottomNavController) },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if(title == "All Courses" && mAuth.currentUser!!.uid == ADMIN_ID) {
                FloatingActionButton(onClick = {
                    mainViewModel.allCoursesToEditCourseDetails(mainNavController)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Courses (Admin Only)")
                }
            }
            else if(title == "My Courses" && mAuth.currentUser!!.uid != ADMIN_ID) {
                FloatingActionButton(onClick = {
                    mainViewModel.showUnenrollDialog()
                }) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = "Unenroll from all courses")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            BottomBarNavGraph(
                mainNavController = mainNavController,
                bottomNavController = bottomNavController,
                mainViewModel = mainViewModel,
                profileViewModel = profileViewModel
            )
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

@Composable
fun CourseUnenrollAllConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    userEnrolledCourseList: List<String>,
    mainViewModel: MainViewModel
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text(if(userEnrolledCourseList.isEmpty())"No Courses Available" else "Unenroll from all Courses")
            },
            text = {
                Text(if(userEnrolledCourseList.isEmpty())"You have not enrolled in any course yet" else "Are you sure you want to unenroll from all courses?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick =  {
                    mainViewModel.unenrollAll(navController, userEnrolledCourseList)
                }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}