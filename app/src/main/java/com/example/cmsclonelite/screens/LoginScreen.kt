package com.example.cmsclonelite.screens

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.R
import com.example.cmsclonelite.Screen
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

private const val CLIENT_ID = "557828460372-0184fqcfulugr78smv592m76u2rsqppm.apps.googleusercontent.com"
//TODO: Make Constants file and move constants there

private lateinit var mAuth: FirebaseAuth
private lateinit var oneTapClient: SignInClient
private lateinit var signInRequest: BeginSignInRequest
private lateinit var signUpRequest: BeginSignInRequest
private var showOneTapUI = true

@Composable
fun LoginScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
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
    val loginResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            try {
                oneTapClient = Identity.getSignInClient(context)
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                when {
                    idToken != null -> {
                        firebaseAuthWithGoogle(idToken, navController, context, db)
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
                            text = "Login with GMail",
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

private fun firebaseAuthWithGoogle(idToken: String, navController: NavHostController, context: Context, db: FirebaseFirestore) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    mAuth.signInWithCredential(credential)
        .addOnCompleteListener(context.findActivity()) { task ->
            if(task.isSuccessful) {
                val currentUserUid = mAuth.currentUser!!.uid
                val userDoc = db.collection("users").document(currentUserUid)
                userDoc.get().addOnCompleteListener { innerTask ->
                    if (innerTask.isSuccessful) {
                        val document = innerTask.result
                        if(document != null) {
                            if (document.exists()) {
                                Log.d(TAG, "Document already exists.")
                            } else {
                                val userInfo = hashMapOf(
                                    "name" to mAuth.currentUser!!.displayName,
                                    "enrolled" to listOf<String>()
                                )
                                db.collection("users").document(currentUserUid).set(userInfo)
                                Log.d(TAG, "Document doesn't exist. Created new one")
                            }
                        }
                    } else {
                        Log.d("TAG", "Error: ", task.exception)
                    }
                }
                Log.d(TAG, "Signed In with Credential")
                navController.navigate(route = Screen.MainScreen.route) {
                    popUpTo(Screen.Login.route) {
                        inclusive = true
                    }
                }
            }
            else {
                Log.w(TAG, "Failed to Sign In with One Tap")
            }
        }

}