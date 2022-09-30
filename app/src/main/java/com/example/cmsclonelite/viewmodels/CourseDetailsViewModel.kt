package com.example.cmsclonelite.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.NavHostController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    private val db: FirebaseFirestore,
    private val mAuth: FirebaseAuth,
    private val courseRepository: CourseRepository
): ViewModel() {
    private val _isDeleteDialog = MutableLiveData<Boolean>()
    val isDeleteDialog: LiveData<Boolean>
        get() = _isDeleteDialog

    private val _isEnrollDialog = MutableLiveData<Boolean>()
    val isEnrollDialog: LiveData<Boolean>
        get() = _isEnrollDialog

    private val _isCalendarDialog = MutableLiveData<Boolean>()
    val isCalendarDialog: LiveData<Boolean>
        get() = _isCalendarDialog

    private val _isUnenrollDialog = MutableLiveData<Boolean>()
    val isUnenrollDialog: LiveData<Boolean>
        get() = _isUnenrollDialog

    private val _userEnrolledCourseList = MutableLiveData<List<String>>()
    val userEnrolledCourseList: LiveData<List<String>>
    get() = _userEnrolledCourseList

    fun initialize() {
        _isDeleteDialog.value = false
        _isEnrollDialog.value = false
        _isCalendarDialog.value = false
        _isUnenrollDialog.value = false
    }

    fun showDeleteDialog() {
        _isDeleteDialog.value = true
    }

    fun removeDeleteDialog() {
        _isDeleteDialog.value = false
    }

    fun showEnrollDialog() {
        _isEnrollDialog.value = true
    }

    fun removeEnrollDialog() {
        _isEnrollDialog.value = false
    }

    fun showCalendarDialog() {
        _isCalendarDialog.value = true
    }

    fun removeCalendarDialog() {
        _isCalendarDialog.value = false
    }

    fun showUnenrollDialog() {
        _isUnenrollDialog.value = true
    }

    fun removeUnenrollDialog() {
        _isUnenrollDialog.value = false
    }

    fun courseDetailsToAnnouncements(navController: NavHostController, course: Course) {
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "courseAnnouncements",
            value = course
        )
        navController.navigate(Screen.Announcements.route)
    }

    fun courseDetailsToEditCourse(navController: NavHostController, course: Course) {
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "courseEdit",
            value = course
        )
        navController.navigate(Screen.EditCourseDetails.route)
    }

    fun getUserEnrolledCourseList() {
        viewModelScope.launch {
            _userEnrolledCourseList.value = courseRepository.userEnrolledCourseList(db, mAuth.currentUser!!.uid)
        }
    }

    fun enrollInCourse(navController: NavHostController, course: Course) {
        courseRepository.enrollInCourse(db, mAuth.currentUser!!.uid, course)
        navController.navigate(Screen.MainScreen.route) {
            popUpTo(Screen.MainScreen.route) {
                inclusive = true
            }
        }
    }

    fun unenrollFromCourse(navController: NavHostController, course: Course) {
        courseRepository.unenrollFromCourse(db, mAuth.currentUser!!.uid, course)
        navController.navigate(Screen.MainScreen.route) {
            popUpTo(Screen.MainScreen.route) {
                inclusive = true
            }
        }
    }

    fun deleteCourse(navController: NavHostController, course: Course) {
        courseRepository.deleteCourse(db, course)
        navController.navigate(Screen.MainScreen.route) {
            popUpTo(Screen.MainScreen.route) {
                inclusive = true
            }
        }
    }

    fun calendarExport(context: Context, navController: NavHostController, course: Course) {
        _isCalendarDialog.value = false
        courseRepository.calendarExport(context, mAuth.currentUser!!.email!!, course)
        Toast.makeText(context, "Calendar Events added", Toast.LENGTH_SHORT).show()
        navController.navigate(Screen.MainScreen.route) {
            popUpTo(Screen.MainScreen.route) {
                inclusive = true
            }
        }
    }
}

class CourseDetailsViewModelFactory(
    private val db: FirebaseFirestore,
    private val mAuth: FirebaseAuth,
    private val courseRepository: CourseRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseDetailsViewModel::class.java)) {
            return CourseDetailsViewModel(db, mAuth, courseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}