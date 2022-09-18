package com.example.cmsclonelite.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import com.example.cmsclonelite.Screen
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth

class SettingsViewModel {
    private val _isDarkTheme = MutableLiveData<Boolean>()
    val isDarkTheme: LiveData<Boolean>
        get() = _isDarkTheme

    fun setDarkTheme() {
        _isDarkTheme.value = true
    }
    fun setLightTheme() {
        _isDarkTheme.value = false
    }
    fun signOut(mAuth: FirebaseAuth, oneTapClient: SignInClient, mainNavController: NavHostController) {
        mAuth.signOut()
        oneTapClient.signOut()
        mainNavController.navigate(route = Screen.Login.route) {
            popUpTo(Screen.MainScreen.route) {
                inclusive = true
            }
        }
    }
}