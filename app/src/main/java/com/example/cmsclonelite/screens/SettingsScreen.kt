package com.example.cmsclonelite.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import com.example.cmsclonelite.settingsViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth

private lateinit var mAuth: FirebaseAuth
private lateinit var oneTapClient: SignInClient

@Composable
fun SettingsScreen(mainNavController: NavHostController) {
    val context = LocalContext.current
    val sharedPrefs = context
        .getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
    val dark = sharedPrefs.getBoolean("darkTheme", false)
    var checked by remember { mutableStateOf(dark) }
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
                settingsViewModel.signOut(mAuth = mAuth, oneTapClient = oneTapClient, mainNavController = mainNavController)
            }
        ) {
            Text(
                text = "Logout",
                fontSize = 15.sp
            )
        }
        Spacer(modifier = Modifier.padding(top = 30.dp))
        Row(modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Enable Dark Theme",
                fontSize = 20.sp)

            Switch(checked = checked,
                onCheckedChange = {
                    sharedPrefs.edit()
                        .putBoolean("dark_theme", it)
                        .apply()
                    checked = it
                    if(it) settingsViewModel.darkTheme() else settingsViewModel.lightTheme()
                })
        }
    }
}
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(rememberNavController())
}