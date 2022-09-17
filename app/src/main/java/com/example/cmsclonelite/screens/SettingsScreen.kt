package com.example.cmsclonelite.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.example.cmsclonelite.Screen
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth

private lateinit var mAuth: FirebaseAuth
private lateinit var oneTapClient: SignInClient

@Composable
fun SettingsScreen(mainNavController: NavHostController) {
    mAuth = FirebaseAuth.getInstance()
    oneTapClient = Identity.getSignInClient(LocalContext.current)
    val user = mAuth.currentUser
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()) {
        Text(text = if (user == null) "" else "${user.displayName}",
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(top = 100.dp))
        Button(
            onClick = {
                mAuth.signOut()
                oneTapClient.signOut()
                mainNavController.navigate(route = Screen.Login.route) {
                    popUpTo(Screen.MainScreen.route) {
                        inclusive = true
                    }
                }
            }
        ) {
            Text(
                text = "Logout",
                fontSize = 15.sp
            )
        }
    }

}

@Composable
@Preview
fun SettingsScreenPreview() {
    SettingsScreen(rememberNavController())
}