package com.example.cmsclonelite.viewmodels

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.screens.findActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

private const val CLIENT_ID = "557828460372-0184fqcfulugr78smv592m76u2rsqppm.apps.googleusercontent.com"

class LoginViewModel(
    private val db: FirebaseFirestore,
    private val mAuth: FirebaseAuth
): ViewModel() {
    val signInRequest = BeginSignInRequest.builder()
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
    val signUpRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build())
        .build()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String>
        get() = _username

    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    private val _passwordVisible = MutableLiveData<Boolean>()
    val passwordVisible: LiveData<Boolean>
        get() = _passwordVisible

    private val _isNoGoogleAccountDialog = MutableLiveData<Boolean>()
    val isNoGoogleAccountDialog: LiveData<Boolean>
        get() = _isNoGoogleAccountDialog

    fun initializeLogin() {
        _isNoGoogleAccountDialog.value = false
    }

    fun initializeAdminLogin() {
        _username.value = ""
        _password.value = ""
        _passwordVisible.value = true
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun togglePassword() {
        _passwordVisible.value = _passwordVisible.value == false
    }

    fun removeNoGoogleAccountAlert() {
        _isNoGoogleAccountDialog.value = false
    }

    fun showNoGoogleAccountAlert() {
        _isNoGoogleAccountDialog.value = true
    }

    fun emailAndPasswordLogin(context: Context, navController: NavController) {
        val email = _username.value!!.lowercase() + "@hyderabad.bits-pilani.ac.in"
        if(_username.value == "") {
            Toast.makeText(context.findActivity(), "Please enter your username",
                Toast.LENGTH_SHORT).show()
        }
        else if(_password.value == "") {
            Toast.makeText(context.findActivity(), "Please enter your password",
                Toast.LENGTH_SHORT).show()
        }
        else {
            firebaseAuthWithEmail(email, _password.value!!, navController, context)
        }
    }

    fun googleLogin(context: Context, idToken: String, navController: NavHostController) {
        firebaseAuthWithGoogle(idToken, navController, context)
    }

    private fun firebaseAuthWithEmail(email: String, password: String, navController: NavController, context: Context) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(context.findActivity()) { task ->
                if (task.isSuccessful) {
                    //To change admin user data, uncomment below code
                        // val profileUpdates = UserProfileChangeRequest.Builder()
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
                        Log.d(ContentValues.TAG, "Signed In with Email")
                        navController.navigate(route = Screen.MainScreen.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                } else {
                    Log.w(ContentValues.TAG, "Failed to Sign In with Email", task.exception)
                    Toast.makeText(
                        context.findActivity(), "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    private fun firebaseAuthWithGoogle(idToken: String, navController: NavHostController, context: Context) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(context.findActivity()) { task ->
                if(task.isSuccessful) {
                    val sharedPrefs = context
                        .getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
                    val fcmToken = sharedPrefs.getString("fcmToken", "")
                    val currentUserUid = mAuth.currentUser!!.uid
                    val userDoc = db.collection("users").document(currentUserUid)
                    userDoc.get().addOnCompleteListener { innerTask ->
                        if (innerTask.isSuccessful) {
                            val document = innerTask.result
                            if(document != null) {
                                if (document.exists()) {
                                    Log.d(ContentValues.TAG, "Document already exists.")
                                } else {
                                    val userInfo = hashMapOf(
                                        "name" to mAuth.currentUser!!.displayName,
                                        "enrolled" to listOf<String>(),
                                        "fcmToken" to fcmToken
                                    )
                                    db.collection("users").document(currentUserUid).set(userInfo)
                                    Log.d(ContentValues.TAG, "Document doesn't exist. Created new one")
                                }
                            }
                        } else {
                            Log.d("TAG", "Error: ", task.exception)
                        }
                    }
                    Log.d(ContentValues.TAG, "Signed In with Credential")
                    navController.navigate(route = Screen.MainScreen.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
                else {
                    Log.w(ContentValues.TAG, "Failed to Sign In with One Tap")
                }
            }

    }
}

class LoginViewModelFactory(
    private val db: FirebaseFirestore,
    private val mAuth: FirebaseAuth
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(db, mAuth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}