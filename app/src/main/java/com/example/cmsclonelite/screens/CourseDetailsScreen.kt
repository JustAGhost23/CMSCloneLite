package com.example.cmsclonelite.screens

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.profileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun CourseDetailsScreen(navController: NavHostController, course: Course) {
    val db = FirebaseFirestore.getInstance()
    mAuth = FirebaseAuth.getInstance()
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showEnrollDialog = remember { mutableStateOf(false) }
    Card {
        if (showDeleteDialog.value) {
            CourseDeletionConfirmation(showDialog = showDeleteDialog.value,
                onDismiss = {showDeleteDialog.value = false},
                navController = navController,
                db = db,
                course = course
            )
        }
        if (showEnrollDialog.value) {
            CourseEnrollConfirmation(showDialog = showEnrollDialog.value,
                onDismiss = {showEnrollDialog.value = false},
                navController = navController,
                db = db,
                course = course,
                user = mAuth.currentUser!!
            )
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(
                        "${course.courseName}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigateUp()
                        }) {
                            Icon(Icons.Rounded.ArrowBack, "")
                        }
                    }
                )
            },
        ) {//TODO: Complete Edit and enroll soon
            if(mAuth.currentUser!!.uid == "HT8sVmAC1tSwkoOVcscEphEWYjS2") {
                FABAnywhere(FabPosition.End, onClick = {
                    showDeleteDialog.value = true
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Courses (Admin Only)")
                }
            }
            else {
                FABAnywhere(FabPosition.End, onClick = {
                    showEnrollDialog.value = true
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Enroll in Course")
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Display Course Details",
                    fontSize = MaterialTheme.typography.h3.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailsScreenPreview() {
    CourseDetailsScreen(rememberNavController(), Course())
}
@Composable
fun CourseDeletionConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    db: FirebaseFirestore,
    course: Course
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Delete Course")
            },
            text = {
                Text(text = "Are you sure you want to delete this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    db.collection("courses").document("${course.id}")
                        .delete()
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e: Exception? -> Log.w(TAG, "Error deleting document", e) }
                    navController.navigate(Screen.MainScreen.route)} ) {
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
@Composable
fun CourseEnrollConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    db: FirebaseFirestore,
    user: FirebaseUser,
    course: Course
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Enroll in Course")
            },
            text = {
                Text(text = "Are you sure you want to enroll in this course?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    db.collection("users").document("${user.uid}")
                        .update("enrolled", FieldValue.arrayUnion("${course.id}"))
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e: Exception? -> Log.w(TAG, "Error updating document", e) }
                    navController.navigate(Screen.MainScreen.route)} ) {
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