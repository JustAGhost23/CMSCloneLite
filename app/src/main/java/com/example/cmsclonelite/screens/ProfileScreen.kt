package com.example.cmsclonelite.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.profileViewModel
import com.example.cmsclonelite.viewmodels.MainViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

const val ADMIN_ID = "HT8sVmAC1tSwkoOVcscEphEWYjS2"

private lateinit var mAuth: FirebaseAuth
private lateinit var oneTapClient: SignInClient

@Composable
fun ProfileScreen(mainNavController: NavHostController, mainViewModel: MainViewModel) {
    val courseRepository = CourseRepository()
    val db = FirebaseFirestore.getInstance()
    mAuth = FirebaseAuth.getInstance()
    oneTapClient = Identity.getSignInClient(LocalContext.current)
    val user = mAuth.currentUser
    val context = LocalContext.current
    val sharedPrefs = context
        .getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
    val dark = sharedPrefs.getBoolean("darkTheme", false)
    var checked by remember { mutableStateOf(dark) }
    var totalCourses by remember { mutableStateOf(0) }
    val showDialog = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        totalCourses = courseRepository.totalCourseCount(db)
        mainViewModel.setTitle("Profile")
    }
    Card {
        if (showDialog.value) {
            LogoutConfirmation(showDialog = showDialog.value,
                onDismiss = {showDialog.value = false},
                mainNavController = mainNavController
            )
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.padding(top = 30.dp))
        Row(
            modifier = Modifier.offset(x = 20.dp),
            verticalAlignment = Alignment.Top
        ) {
            val imageUrl = mAuth.currentUser?.photoUrl
            AsyncImage(
                model = if (imageUrl == null) "https://lh3.googleusercontent.com/a/ALm5wu2lBRiENcoc643W_odk7f3cK7MpnTuRWsh3nsV3=s96-c" else imageUrl,
                contentDescription = "User Image"
            )
        }
        Spacer(modifier = Modifier.padding(top = 20.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = if (user == null) "" else "${user.displayName}",
                    textAlign = TextAlign.Start,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.padding(top = 40.dp))
            Row(modifier = Modifier
                .fillMaxWidth(0.9f)
            ) {
                Icon(imageVector = Icons.Default.ListAlt, contentDescription = "All Courses")
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Text(
                    text = "Total Courses Available : ${totalCourses}",
                    fontSize = 20.sp
                )
            }
            if(user != null) {
                if (user.uid != ADMIN_ID) {
                    Spacer(modifier = Modifier.padding(top = 40.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    ) {
                        Icon(imageVector = Icons.Rounded.List, contentDescription = "My Courses")
                        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                        Text(
                            text = "Courses enrolled : 0",
                            fontSize = 20.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 40.dp))
            Row(modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(
                    onClick = {
                        mainNavController.navigate(Screen.About.route)
                    })
            ) {
                Icon(imageVector = Icons.Rounded.Info, contentDescription = "About")
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Text(
                    text = "About",
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.padding(top = 40.dp))
            Row(modifier = Modifier.fillMaxWidth(fraction = 0.9f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Rounded.DarkMode,
                    contentDescription = "Dark Mode")
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Text(text = "Enable Dark Mode",
                    fontSize = 20.sp)
                Spacer(modifier = Modifier.padding(horizontal = 55.dp))
                Switch(checked = checked,
                    onCheckedChange = {
                        sharedPrefs.edit()
                            .putBoolean("darkTheme", it)
                            .apply()
                        checked = it
                        if(it) profileViewModel.setDarkTheme() else profileViewModel.setLightTheme()
                    })
            }
            Spacer(modifier = Modifier.padding(top = 40.dp))
            Row(modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(
                    onClick = {
                        showDialog.value = true
                    })
            ) {
                Icon(imageVector = Icons.Rounded.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Text(text = "Logout",
                    fontSize = 20.sp)
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val mainViewModel = MainViewModel()
    ProfileScreen(rememberNavController(), mainViewModel = mainViewModel)
}
@Composable
fun LogoutConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    mainNavController: NavHostController
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Logout")
            },
            text = {
                Text(text = "Are you sure you want to logout?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    profileViewModel.signOut(
                        mAuth = mAuth,
                        oneTapClient = oneTapClient,
                        mainNavController = mainNavController)} ) {
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