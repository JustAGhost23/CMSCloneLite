package com.example.cmsclonelite.viewmodels

import androidx.lifecycle.*
import androidx.navigation.NavHostController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.screens.ADMIN_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainViewModel(
    private val db: FirebaseFirestore,
    private val mAuth: FirebaseAuth,
    private val courseRepository: CourseRepository
): ViewModel() {
    private val _isUnenrollAllDialog = MutableLiveData<Boolean>()
    val isUnenrollAllDialog: LiveData<Boolean>
        get() = _isUnenrollAllDialog

    private var _screenTitle = MutableLiveData("")
    val screenTitle: LiveData<String>
        get() = _screenTitle

    private var _enrolledCourseList = MutableLiveData<List<Course>>()
    val enrolledCourseList: LiveData<List<Course>>
        get() = _enrolledCourseList

    private var _allCoursesList = MutableLiveData<List<Course>>()
    val allCoursesList: LiveData<List<Course>>
        get() = _allCoursesList

    private var _enrolledCourseIdList = MutableLiveData<List<String>>()
    val enrolledCourseIdList: LiveData<List<String>>
        get() = _enrolledCourseIdList

    init {
        _isUnenrollAllDialog.value = false
    }

    fun setTitle(newTitle: String) {
        _screenTitle.value = newTitle
    }

    fun showUnenrollDialog() {
        _isUnenrollAllDialog.value = true
    }

    fun removeUnenrollDialog() {
        _isUnenrollAllDialog.value = false
    }

    fun getCoursesEnrolledList() {
        viewModelScope.launch {
            _enrolledCourseList.value = courseRepository.getUserEnrolledCoursesData(db, mAuth.currentUser!!.uid)
        }
    }

    fun getAllCoursesList() {
        viewModelScope.launch {
            _allCoursesList.value = courseRepository.getData(db)
        }
    }

    fun getCourseEnrollIdList() {
        viewModelScope.launch {
            if(mAuth.currentUser != null && mAuth.currentUser!!.uid != ADMIN_ID) {
                _enrolledCourseIdList.value =
                    courseRepository.userEnrolledCourseList(db, mAuth.currentUser!!.uid)
            }
        }
    }

    fun myCoursesToEnrolledCourse(navController:NavHostController, course: Course) {
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "course",
            value = course
        )
        navController.navigate(Screen.EnrolledCourseDetails.route)
    }

    fun allCoursesToCourseDetails(navController: NavHostController, course: Course, userEnrolledCourseIdList: List<String>) {
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "course",
            value = course
        )
        if(course.id.toString() in userEnrolledCourseIdList) {
            navController.navigate(Screen.EnrolledCourseDetails.route)
        }
        else {
            navController.navigate(Screen.CourseDetails.route)
        }
    }

    fun allCoursesToEditCourseDetails(navController: NavHostController) {
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "courseEdit",
            value = Course()
        )
        navController.navigate(Screen.EditCourseDetails.route)
    }

    fun unenrollAll(navController: NavHostController, userEnrolledCourseIdList: List<String>) {
        viewModelScope.launch {
            if (userEnrolledCourseIdList.isEmpty()) {
                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.MainScreen.route) {
                        inclusive = true
                    }
                }
            } else {
                for (courseId in userEnrolledCourseIdList) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(courseId)
                }
                viewModelScope.launch {
                    courseRepository.unenrollAll(db, mAuth.currentUser!!.uid)
                }
                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.MainScreen.route) {
                        inclusive = true
                    }
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val db: FirebaseFirestore,
    private val mAuth: FirebaseAuth,
    private val courseRepository: CourseRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(db, mAuth, courseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}