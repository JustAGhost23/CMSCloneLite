package com.example.cmsclonelite.screens

import android.app.Activity
import android.content.*
import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.R
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth
private lateinit var oneTapClient: SignInClient
private lateinit var signInRequest: BeginSignInRequest
private lateinit var signUpRequest: BeginSignInRequest
private var showOneTapUI = true

@Composable
fun LoginScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel
) {
    val context = LocalContext.current
    mAuth = FirebaseAuth.getInstance()
    oneTapClient = Identity.getSignInClient(context)
    signInRequest = loginViewModel.signInRequest
    signUpRequest = loginViewModel.signUpRequest
    val showNoGoogleAlertDialog: Boolean by loginViewModel.isNoGoogleAccountDialog.observeAsState(false)
    loginViewModel.initializeLogin()
    val loginResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            try {
                oneTapClient = Identity.getSignInClient(context)
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                when {
                    idToken != null -> {
                        loginViewModel.googleLogin(context, idToken, navController)
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
                    Log.d(
                        TAG, "Couldn't get credential from result." +
                                " (${e.localizedMessage})")
                }
            }
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Card {
            if (showNoGoogleAlertDialog) {
                NoGoogleAccountAlert(showDialog = showNoGoogleAlertDialog,
                    onDismiss = { loginViewModel.removeNoGoogleAccountAlert()})
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(top = 180.dp))
            Text(
                "BPHC CMS Clone Lite",
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(top = 50.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painterResource(id = R.drawable.bitslogo),
                    contentDescription = "Bits Logo",
                    modifier = Modifier.fillMaxWidth(0.7f)
                        .fillMaxHeight(0.4f),
                )
            }
            Spacer(Modifier.padding(vertical = 30.dp))
            Button(
                onClick = {
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(context.findActivity()) { result ->
                            try {
                                loginResultLauncher.launch(
                                    IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                                        .build()
                                )
                            } catch (e: IntentSender.SendIntentException) {
                                Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                            }
                        }
                        .addOnFailureListener(context.findActivity()) { e: Exception? ->
                            e?.localizedMessage?.let { Log.d(TAG, it) }
                            oneTapClient.beginSignIn(signUpRequest)
                                .addOnSuccessListener(context.findActivity()) { result ->
                                    try {
                                        loginResultLauncher.launch(
                                            IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                                                .build()
                                        )
                                    } catch (e: IntentSender.SendIntentException) {
                                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                                    }
                                }
                                .addOnFailureListener(context.findActivity()) { e: Exception ->
                                    loginViewModel.showNoGoogleAccountAlert()
                                    e.localizedMessage?.let { Log.d(TAG, it) }
                                }
                        }
                }
            ) {
                Box(
                contentAlignment = Alignment.Center
            ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            modifier = Modifier
                                .size(20.dp),
                            contentDescription = "Google Icon",
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Login with GMail",
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 12.dp))
            Button(
                onClick = {
                    navController.navigate(route = Screen.AdminLogin.route)
                }
            ) {
                Text(text = "Admin Login",
                    fontSize = 15.sp)
            }
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    val db = FirebaseFirestore.getInstance()
    mAuth = FirebaseAuth.getInstance()
    val loginViewModel = LoginViewModel(db, mAuth)
    LoginScreen(rememberNavController(), loginViewModel)
}
@Composable
fun NoGoogleAccountAlert(showDialog: Boolean,
          onDismiss: () -> Unit) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("No Google Account")
            },
            text = {
                Text(text = "No Google Accounts Found on this device")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss ) {
                    Text("OK")
                }
            },
            dismissButton = {}
        )
    }
}
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}