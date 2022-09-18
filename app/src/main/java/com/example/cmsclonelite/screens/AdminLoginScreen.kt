package com.example.cmsclonelite.screens

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Screen
import com.google.firebase.auth.FirebaseAuth


private lateinit var mAuth: FirebaseAuth

@Composable
fun AdminLoginScreen(
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        val context = LocalContext.current
        mAuth = FirebaseAuth.getInstance()
        val focusManager = LocalFocusManager.current
        var username by rememberSaveable { mutableStateOf("")}
        var password by rememberSaveable { mutableStateOf("")}
        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        //Uncomment this and AlertDialog below and make changes to use the AlertDialog if needed
        //val showDialog = remember { mutableStateOf(false) }
        //Card {
        //    if (showDialog.value) {
        //        WrongCredentialsAlert(showDialog = showDialog.value,
        //            onDismiss = {showDialog.value = false})
        //    }
        //}
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.padding(top = 200.dp))
            Text(text = "Admin Login",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.padding(top = 100.dp))
            TextField(value = username,
                colors = TextFieldDefaults.textFieldColors(),
                label = { Text("Username") },
                placeholder = { Text("Enter Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                leadingIcon = {
                    val image = Icons.Rounded.Person
                    Icon(imageVector = image, contentDescription = "User Icon")
                },
                onValueChange = {
                    username = it
                })
            Spacer(modifier = Modifier.padding(top = 20.dp))
            TextField(value = password,
                colors = TextFieldDefaults.textFieldColors(),
                label = { Text("Password") },
                placeholder = { Text("Enter Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                leadingIcon = {
                    val image = Icons.Rounded.Lock
                    Icon(imageVector = image, contentDescription = "Password Icon")
                },
                trailingIcon = {
                    val image = if (passwordVisible) {
                        Icons.Rounded.VisibilityOff
                    }
                    else {
                        Icons.Rounded.Visibility
                    }
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }){
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                onValueChange = {
                    password = it
                })
            Spacer(modifier = Modifier.padding(top = 100.dp))
            Button(
                onClick = {
                    val email = username.lowercase() + "@domain.com"
                    firebaseAuthWithEmail(email, password, navController, context)

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
fun AdminLoginPreview() {
    AdminLoginScreen(rememberNavController())
}
//Composable to make an AlertDialog
//@Composable
//fun WrongCredentialsAlert(showDialog: Boolean,
//          onDismiss: () -> Unit) {
//    if (showDialog) {
//        AlertDialog(
//            title = {
//                Text("Incorrect Credentials")
//            },
//            text = {
//                Text(text = "Please check the username or password again")
//            },
//            onDismissRequest = onDismiss,
//            confirmButton = {
//                TextButton(onClick = onDismiss ) {
//                    Text("OK")
//                }
//            },
//            dismissButton = {}
//        )
//    }
//}
private fun firebaseAuthWithEmail(email: String, password: String, navController: NavController, context: Context) {
    mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(context.findActivity()) { task ->
            if (task.isSuccessful) {
                //To change admin user data, uncomment below code
                //val profileUpdates = UserProfileChangeRequest.Builder()
                //    .setDisplayName("Admin")
                //    .setPhotoUri(Uri.parse("https://lh3.googleusercontent.com/a/ALm5wu2lBRiENcoc643W_odk7f3cK7MpnTuRWsh3nsV3=s96-c"))
                //    .build()
                //val user = mAuth.currentUser
                //user!!.updateProfile(profileUpdates)
                //    .addOnCompleteListener { task ->
                //        if (task.isSuccessful) {
                //            Log.d(TAG, "User profile updated.")
                //        }
                //    }
                Log.d(TAG, "Signed In with Email")
                navController.navigate(route = Screen.MainScreen.route) {
                    popUpTo(Screen.Login.route) {
                        inclusive = true
                    }
                }
            } else {
                Log.w(TAG, "Failed to Sign In with Email", task.exception)
                Toast.makeText(context.findActivity(), "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }
}