package com.example.cmsclonelite.screens

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.IntentSender
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.R
import com.example.cmsclonelite.Screen
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception


private const val CLIENT_ID = "557828460372-0184fqcfulugr78smv592m76u2rsqppm.apps.googleusercontent.com"
private const val REQ_ONE_TAP = 2
//TODO: Make Constants file and move constants there

private lateinit var mAuth: FirebaseAuth
private lateinit var oneTapClient: SignInClient
private lateinit var signInRequest: BeginSignInRequest
private lateinit var signUpRequest: BeginSignInRequest

@Composable
fun LoginScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    mAuth = FirebaseAuth.getInstance()
    oneTapClient = Identity.getSignInClient(LocalContext.current)
    signInRequest = BeginSignInRequest.builder()
        .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
            .setSupported(true)
            .build())
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build())
        .build()
    signUpRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build())
        .build()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        val showDialog = remember { mutableStateOf(false) }
        Card {
            if (showDialog.value) {
                NoGoogleAccountAlert(showDialog = showDialog.value,
                    onDismiss = {showDialog.value = false})
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(top = 300.dp))
            Text(
                "BPHC CMS Clone Lite",
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(top = 150.dp))
            Button(
                onClick = {
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(context.findActivity()) { result ->
                            try {
                                startIntentSenderForResult(context.findActivity(),
                                    result.pendingIntent.intentSender, REQ_ONE_TAP,
                                    null, 0, 0, 0, null)
                            } catch (e: IntentSender.SendIntentException) {
                                Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                            }
                        }
                        .addOnFailureListener(context.findActivity()) { e: Exception? ->
                            e?.localizedMessage?.let { Log.d(TAG, it) }
                            oneTapClient.beginSignIn(signUpRequest)
                                .addOnSuccessListener(context.findActivity()) { result ->
                                    try {
                                        startIntentSenderForResult(context.findActivity(),
                                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                                            null, 0, 0, 0, null)
                                    } catch (e: IntentSender.SendIntentException) {
                                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                                    }
                                }
                                .addOnFailureListener(context.findActivity()) { e: Exception ->
                                    showDialog.value = true
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
                            text = "Login with BITSMail",
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
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
    LoginScreen(rememberNavController())
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