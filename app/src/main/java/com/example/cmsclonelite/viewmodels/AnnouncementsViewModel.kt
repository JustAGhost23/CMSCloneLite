package com.example.cmsclonelite.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.NavHostController
import com.example.cmsclonelite.Announcement
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.screens.findActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AnnouncementsViewModel(
    private val db: FirebaseFirestore,
    private val mAuth: FirebaseAuth,
    private val courseRepository: CourseRepository
): ViewModel() {
    private var _allAnnouncementsList = MutableLiveData<List<Announcement>>()
    val allAnnouncementsList: LiveData<List<Announcement>>
        get() = _allAnnouncementsList

    private val _isAddAnnouncementDialog = MutableLiveData<Boolean>()
    val isAddAnnouncementDialog: LiveData<Boolean>
        get() = _isAddAnnouncementDialog

    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private val _body = MutableLiveData<String>()
    val body: LiveData<String>
        get() = _body

    fun initialize() {
        _title.value = ""
        _body.value = ""
        _isAddAnnouncementDialog.value = false
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setBody(body: String) {
        _body.value = body
    }

    fun showAddAnnouncementDialog() {
        _isAddAnnouncementDialog.value = true
    }

    fun removeAddAnnouncementDialog() {
        _isAddAnnouncementDialog.value = false
    }

    fun getAllAnnouncementsList(courseId: String) {
        viewModelScope.launch {
            _allAnnouncementsList.value = courseRepository.getAnnouncements(db, courseId)
        }
    }

    fun allAnnouncementsToAddCourseDetails(navController: NavHostController, course: Course) {
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "courseAnnouncements",
            value = course
        )
        navController.navigate(Screen.AddAnnouncements.route)
    }

    fun postAnnouncement(context: Context, navController: NavHostController, course: Course) {
        if(_title.value == "") {
            Toast.makeText(context.findActivity(), "Please enter the title of the announcement",
                Toast.LENGTH_SHORT).show()
        }
        else if(_body.value == "") {
            Toast.makeText(context.findActivity(), "Please enter the body of the announcement",
                Toast.LENGTH_SHORT).show()
        }
        else {
            courseRepository.sendPushNotification(
                course,
                Announcement(_title.value, _body.value)
            )
            courseRepository.addAnnouncement(
                db,
                course.id!!,
                Announcement(_title.value, _body.value)
            )
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(Screen.MainScreen.route) {
                    inclusive = true
                }
            }
        }
    }
}

class AnnouncementsViewModelFactory(
    private val db: FirebaseFirestore,
    private val mAuth: FirebaseAuth,
    private val courseRepository: CourseRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnnouncementsViewModel::class.java)) {
            return AnnouncementsViewModel(db, mAuth, courseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}