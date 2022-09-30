package com.example.cmsclonelite.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.screens.findActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditCourseDetailsViewModel(
    private val db: FirebaseFirestore,
    private val courseRepository: CourseRepository
): ViewModel() {
    private val _isAddCourseDialog = MutableLiveData<Boolean>()
    val isAddCourseDialog: LiveData<Boolean>
        get() = _isAddCourseDialog

    private val _isEditCourseDialog = MutableLiveData<Boolean>()
    val isEditCourseDialog: LiveData<Boolean>
        get() = _isEditCourseDialog

    private val _courseName = MutableLiveData<String>()
    val courseName: LiveData<String>
        get() = _courseName

    private val _instructor = MutableLiveData<String>()
    val instructor: LiveData<String>
        get() = _instructor

    private val _startDateStartTime = MutableLiveData<Date>()
    val startDateStartTime: LiveData<Date>
        get() = _startDateStartTime

    private val _endDateEndTime = MutableLiveData<Date>()
    val endDateEndTime: LiveData<Date>
        get() = _endDateEndTime

    fun initialize(course: Course) {
        if(course.id == null) {
            _courseName.value = ""
            _instructor.value = ""
        }
        else {
            _courseName.value = course.courseName
            _instructor.value = course.instructor
            _startDateStartTime.value = course.startDateStartTime
            _endDateEndTime.value = course.endDateEndTime
        }
        _isAddCourseDialog.value = false
        _isEditCourseDialog.value = false
    }

    fun showAddCourseDialog() {
        _isAddCourseDialog.value = true
    }

    fun removeAddCourseDialog() {
        _isAddCourseDialog.value = false
    }

    fun showEditCourseDialog() {
        _isEditCourseDialog.value = true
    }

    fun removeEditCourseDialog() {
        _isEditCourseDialog.value = false
    }

    fun setCourseName(course: Course, courseName: String) {
        _courseName.value = courseName
        course.courseName = courseName
    }

    fun setInstructor(course: Course, instructor: String) {
        _instructor.value = instructor
        course.instructor = instructor
    }

    fun addEndDateEndTime(endDateEndTime: Date, course: Course) {
        course.endDateEndTime = endDateEndTime
    }

    fun addStartDateStartTime(startDateStartTime: Date, course: Course) {
        course.startDateStartTime = startDateStartTime
    }

    fun postCourseToDatabase(context: Context, navController: NavHostController, course: Course) {
        if(course.id == null) {
            if((course.courseName != "" || course.courseName != null) && (course.instructor != "" || course.instructor != null) && (course.days != "" || course.days != null) && course.startDateStartTime != null && course.endDateEndTime != null && (course.endDateEndTime!! > course.startDateStartTime!!)) {
                courseRepository.addCourse(db, course)
                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.MainScreen.route) {
                        inclusive = true
                    }
                }
            }
            else if(course.courseName == null || course.courseName == "") {
                Toast.makeText(context.findActivity(), "Please enter the name of the course",
                    Toast.LENGTH_SHORT).show()
            }
            else if(course.instructor == null || course.instructor == "") {
                Toast.makeText(context.findActivity(), "Please enter the name of the instructor",
                    Toast.LENGTH_SHORT).show()
            }
            else if(course.days == null || course.days == "") {
                Toast.makeText(context.findActivity(), "Please choose the days on which the classes are to be held",
                    Toast.LENGTH_SHORT).show()
            }
            else if(course.startDateStartTime == null || course.endDateEndTime == null) {
                Toast.makeText(context.findActivity(), "Please enter the course timings",
                    Toast.LENGTH_SHORT).show()
            }
            else if(course.endDateEndTime!! <= course.startDateStartTime!!) {
                Toast.makeText(context.findActivity(), "Selected Course Timings are invalid",
                    Toast.LENGTH_SHORT).show()
            }
        }
        else {
            if(course.courseName != "" && course.instructor != "" && course.days != "" && (course.endDateEndTime!! > course.startDateStartTime!!)) {
                courseRepository.editCourse(db, course.id!!, course)
                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.MainScreen.route) {
                        inclusive = true
                    }
                }
            }
            else if(course.courseName == null || course.courseName == "") {
                Toast.makeText(context.findActivity(), "Please enter the name of the course",
                    Toast.LENGTH_SHORT).show()
            }
            else if(course.instructor == null || course.instructor == "") {
                Toast.makeText(context.findActivity(), "Please enter the name of the instructor",
                    Toast.LENGTH_SHORT).show()
            }
            else if(course.days == null || course.days == "") {
                Toast.makeText(context.findActivity(), "Please choose the days on which the classes are to be held",
                    Toast.LENGTH_SHORT).show()
            }
            else if(course.endDateEndTime!! <= course.startDateStartTime!!) {
                Toast.makeText(context.findActivity(), "Selected Course Timings are invalid",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class EditCourseDetailsViewModelFactory(
    private val db: FirebaseFirestore,
    private val courseRepository: CourseRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditCourseDetailsViewModel::class.java)) {
            return EditCourseDetailsViewModel(db, courseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}