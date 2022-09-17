package com.example.cmsclonelite

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.graphs.SetupNavGraph
import com.example.cmsclonelite.ui.theme.CMSCloneLiteTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var mAuth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private val REQ_ONE_TAP = 2
    private var showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        setContent {
            CMSCloneLiteTheme {
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
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    oneTapClient = Identity.getSignInClient(this)
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            firebaseAuthWithGoogle(idToken)
                            Log.d(TAG, "Got ID token.")
                        }
                        else -> {
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d(TAG, "One-tap dialog was closed.")
                        showOneTapUI = false
                    }
                    CommonStatusCodes.NETWORK_ERROR -> {
                        Log.d(TAG, "One-tap encountered a network error.")
                    }
                    else -> {
                        Log.d(TAG, "Couldn't get credential from result." +
                                " (${e.localizedMessage})")
                    }
                }
                }
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    Log.d(TAG, "Signed In with Credential")
                    navController.navigate(route = Screen.MainScreen.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
                else {
                    Log.w(TAG, "Failed to Sign In")
                }
            }
    }
}