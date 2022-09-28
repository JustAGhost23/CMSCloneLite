package com.example.cmsclonelite.screens

import android.content.ContentValues
import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.BottomBarScreen
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.graphs.BottomBarNavGraph
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private lateinit var mAuth: FirebaseAuth

@Composable
fun MainScreen(mainNavController: NavHostController, mainViewModel: MainViewModel = viewModel()) {
    val db = FirebaseFirestore.getInstance()
    mAuth = FirebaseAuth.getInstance()
    val courseRepository = CourseRepository()
    var userEnrolledCourseList: List<String>? by remember { mutableStateOf(mutableListOf()) }
    val showUnenrollAllDialog = remember { mutableStateOf(false) }
    val title: String by mainViewModel.screenTitle.observeAsState("")
    val bottomNavController = rememberNavController()
    LaunchedEffect(Unit) {
        userEnrolledCourseList = courseRepository.userEnrolledCourseList(db, mAuth.currentUser!!.uid)
    }
    Card {
        if(userEnrolledCourseList != null) {
            if (showUnenrollAllDialog.value) {
                CourseUnenrollAllConfirmation(
                    showDialog = showUnenrollAllDialog.value,
                    onDismiss = { showUnenrollAllDialog.value = false },
                    navController = mainNavController,
                    db = db,
                    user = mAuth.currentUser!!,
                    userEnrolledCourseList = userEnrolledCourseList!!
                )
            }
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
                    mainNavController.currentBackStackEntry?.savedStateHandle?.set(
                        key = "courseEdit",
                        value = Course()
                    )
                    mainNavController.navigate(Screen.EditCourseDetails.route)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Courses (Admin Only)")
                }
            }
            else if(title == "My Courses" && mAuth.currentUser!!.uid != ADMIN_ID) {
                FloatingActionButton(onClick = {
                    showUnenrollAllDialog.value = true
                }) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = "Unenroll from all courses")
                }
            }
        }
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

@Composable
fun CourseUnenrollAllConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    db: FirebaseFirestore,
    user: FirebaseUser,
    userEnrolledCourseList: List<String>
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
                    if(userEnrolledCourseList.isEmpty()) {
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.MainScreen.route) {
                                inclusive = true
                            }
                        }
                    }
                    else {
                        for (course in userEnrolledCourseList) {
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(course)
                        }
                        db.collection("users").document("${user.uid}")
                            .update("enrolled", listOf<String>())
                            .addOnSuccessListener {
                                Log.d(
                                    ContentValues.TAG,
                                    "DocumentSnapshot successfully updated!"
                                )
                            }
                            .addOnFailureListener { e: Exception? ->
                                Log.w(ContentValues.TAG, "Error updating document", e)
                            }
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.MainScreen.route) {
                                inclusive = true
                            }
                        }
                    }
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