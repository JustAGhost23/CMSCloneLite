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
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.MainViewModel
import com.example.cmsclonelite.viewmodels.ProfileViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth
private lateinit var oneTapClient: SignInClient

@Composable
fun ProfileScreen(
    mainNavController: NavHostController,
    mainViewModel: MainViewModel,
    profileViewModel: ProfileViewModel
) {
    LaunchedEffect(Unit) {
        mainViewModel.setTitle("Profile")
    }
    val context = LocalContext.current
    mAuth = FirebaseAuth.getInstance()
    oneTapClient = Identity.getSignInClient(context)
    val sharedPrefs = context
        .getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
    val dark = sharedPrefs.getBoolean("darkTheme", false)
    var checked by remember { mutableStateOf(dark) }
    val showLogoutDialog: Boolean by profileViewModel.isLogoutDialog.observeAsState(false)
    val totalCourses: Int by profileViewModel.totalCourses.observeAsState(0)
    val enrolledCourses: Int by profileViewModel.enrolledCourses.observeAsState(0)
    profileViewModel.getTotalCourses()
    profileViewModel.getEnrolledCourses()
    profileViewModel.initialize()

    Card {
        if (showLogoutDialog) {
            LogoutConfirmation(
                showDialog = showLogoutDialog,
                onDismiss = { profileViewModel.removeLogoutConfirmation()},
                mainNavController = mainNavController,
                profileViewModel = profileViewModel
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
                    text = if (mAuth.currentUser == null) "" else "${mAuth.currentUser!!.displayName}",
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
                    text = "Total Courses Available : $totalCourses",
                    fontSize = 20.sp
                )
            }
            if(mAuth.currentUser != null) {
                if (mAuth.currentUser!!.uid != ADMIN_ID) {
                    Spacer(modifier = Modifier.padding(top = 40.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    ) {
                        Icon(imageVector = Icons.Rounded.List, contentDescription = "My Courses")
                        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                        Text(
                            text = "Courses enrolled : $enrolledCourses",
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
                        profileViewModel.showLogoutConfirmation()
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
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance()
    val courseRepository = CourseRepository()
    val mainViewModel = MainViewModel(db, mAuth, courseRepository)
    val profileViewModel = ProfileViewModel()
    ProfileScreen(rememberNavController(), mainViewModel, profileViewModel)
}
@Composable
fun LogoutConfirmation(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    mainNavController: NavHostController,
    profileViewModel: ProfileViewModel
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
                        mainNavController = mainNavController
                    )} ) {
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