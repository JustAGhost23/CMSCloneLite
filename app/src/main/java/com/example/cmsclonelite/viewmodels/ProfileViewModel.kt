package com.example.cmsclonelite.viewmodels

import androidx.lifecycle.*
import androidx.navigation.NavHostController
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val courseRepository = CourseRepository()

    private val _isLogoutDialog = MutableLiveData<Boolean>()
    val isLogoutDialog: LiveData<Boolean>
        get() = _isLogoutDialog

    private val _totalCourses = MutableLiveData<Int>()
    val totalCourses: LiveData<Int>
        get() = _totalCourses

    private val _enrolledCourses = MutableLiveData<Int>()
    val enrolledCourses: LiveData<Int>
        get() = _enrolledCourses

    private val _isDarkTheme = MutableLiveData<Boolean>()
    val isDarkTheme: LiveData<Boolean>
        get() = _isDarkTheme

    init {
        _isLogoutDialog.value = false
    }

    fun showLogoutConfirmation() {
        _isLogoutDialog.value = true
    }

    fun removeLogoutConfirmation() {
        _isLogoutDialog.value = false
    }

    fun getTotalCourses() {
        viewModelScope.launch {
            _totalCourses.value = courseRepository.totalCourseCount(db)
        }
    }

    fun getEnrolledCourses() {
        viewModelScope.launch {
            if(mAuth.currentUser != null) {
                _enrolledCourses.value =
                    courseRepository.userTotalEnrolledCourseCount(db, mAuth.currentUser!!.uid)
            }
        }
    }

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

class ProfileViewModelFactory: ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}