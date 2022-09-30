package com.example.cmsclonelite.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.cmsclonelite.viewmodels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun AdminLoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    mAuth = FirebaseAuth.getInstance()
    val username by loginViewModel.username.observeAsState("")
    val password by loginViewModel.password.observeAsState("")
    val passwordVisible by loginViewModel.passwordVisible.observeAsState(true)
    loginViewModel.initializeAdminLogin()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
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
                    loginViewModel.setUsername(it)
                })
            Spacer(modifier = Modifier.padding(top = 20.dp))
            TextField(value = password,
                colors = TextFieldDefaults.textFieldColors(),
                label = { Text("Password") },
                placeholder = { Text("Enter Password") },
                singleLine = true,
                visualTransformation = if (!passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                    val image = if (!passwordVisible) {
                        Icons.Rounded.VisibilityOff
                    }
                    else {
                        Icons.Rounded.Visibility
                    }
                    val description = "Password Toggle"
                    IconButton(onClick = {
                        loginViewModel.togglePassword()
                    }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                onValueChange = {
                    loginViewModel.setPassword(it)
                })
            Spacer(modifier = Modifier.padding(top = 100.dp))
            Button(
                onClick = {
                    loginViewModel.emailAndPasswordLogin(context, navController)
                }
            ) {
                Text(text = "Admin Login",
                fontSize = 16.sp)
            }
        }
    }
}

@Preview
@Composable
fun AdminLoginPreview() {
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance()
    val loginViewModel = LoginViewModel(db, mAuth)
    AdminLoginScreen(rememberNavController(), loginViewModel)
}