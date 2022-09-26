package com.example.cmsclonelite

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.graphs.SetupNavGraph
import com.example.cmsclonelite.ui.theme.CMSCloneLiteTheme
import com.example.cmsclonelite.viewmodels.*
import com.google.firebase.auth.FirebaseAuth


val profileViewModel = ProfileViewModel()

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        //val application = requireNotNull(this).application
        val sharedPrefs: SharedPreferences = getSharedPreferences("PREFERENCES", MODE_PRIVATE)
        val dark = sharedPrefs.getBoolean("darkTheme", false)
        setContent {
            val darkTheme: Boolean by profileViewModel.isDarkTheme.observeAsState(dark)
            CMSCloneLiteTheme(darkTheme = darkTheme){
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                navController = rememberNavController()
                SetupNavGraph(navController = navController)
                if(user == null) {
                    navController.navigate(route = Screen.Login.route) {
                        popUpTo(Screen.MainScreen.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}