package com.example.cmsclonelite.viewmodels

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.NavHostController
import com.example.cmsclonelite.Announcement
import com.example.cmsclonelite.Course
import com.example.cmsclonelite.Screen
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.screens.findActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnnouncementsViewModel(
    private val db: FirebaseFirestore,
    private val courseRepository: CourseRepository
): ViewModel() {
    private var _allAnnouncementsList = MutableLiveData<List<Announcement>>()
    val allAnnouncementsList: LiveData<List<Announcement>>
        get() = _allAnnouncementsList

    private val _isAddAnnouncementDialog = MutableLiveData<Boolean>()
    val isAddAnnouncementDialog: LiveData<Boolean>
        get() = _isAddAnnouncementDialog

    private val _isFileDeleteDialog = MutableLiveData<Boolean>()
    val isFileDeleteDialog: LiveData<Boolean>
        get() = _isFileDeleteDialog

    private val _storageMetadata = MutableLiveData<StorageMetadata>()
    val storageMetadata: LiveData<StorageMetadata>
        get() = _storageMetadata

    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private val _body = MutableLiveData<String>()
    val body: LiveData<String>
        get() = _body
    
    private val _fileUri = MutableLiveData<Uri?>()
    val fileUri: LiveData<Uri?>
        get() = _fileUri

    private val _downloadUri = MutableLiveData<Uri?>()
    val downloadUri: LiveData<Uri?>
        get() = _downloadUri

    fun initialize() {
        _title.value = ""
        _body.value = ""
        _fileUri.value = null
        _downloadUri.value = null
        _storageMetadata.value = storageMetadata {}
        _isAddAnnouncementDialog.value = false
        _isFileDeleteDialog.value = false
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setBody(body: String) {
        _body.value = body
    }

    fun setFileUri(fileUri: Uri?) {
        _fileUri.value = fileUri
    }

    fun showAddAnnouncementDialog() {
        _isAddAnnouncementDialog.value = true
    }

    fun removeAddAnnouncementDialog() {
        _isAddAnnouncementDialog.value = false
    }

    fun showFileDeleteDialog() {
        _isFileDeleteDialog.value = true
    }

    fun removeFileDeleteDialog() {
        _isFileDeleteDialog.value = false
    }

    fun getAllAnnouncementsList(courseId: String) {
        viewModelScope.launch {
            _allAnnouncementsList.value = courseRepository.getAnnouncements(db, courseId)
        }
    }

    fun allAnnouncementsToDetailedAnnouncement(announcement: Announcement, navController:NavHostController) {
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "announcement",
            value = announcement
        )
        navController.navigate(Screen.DetailedAnnouncement.route)
    }

    fun allAnnouncementsToAddCourseDetails(navController: NavHostController, course: Course) {
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "courseAnnouncements",
            value = course
        )
        navController.navigate(Screen.AddAnnouncements.route)
    }

    fun uploadFileToFirebase(course: Course, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            _downloadUri.postValue(courseRepository.uploadFileToFirebase(course, _fileUri.value!!, context))
            _storageMetadata.postValue(courseRepository.getFileMetadataFromFirebase(course, _fileUri.value!!, context))
        }
    }

    fun deleteFileFromFirebase(course: Course, fileUri: Uri, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            courseRepository.deleteFileFromFirebase(course, fileUri, context)
        }
        _fileUri.value = null
        _downloadUri.value = null
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
                Announcement(_title.value, _body.value, _storageMetadata.value?.name.toString(), _downloadUri.value)
            )
            courseRepository.addAnnouncement(
                db,
                course.id!!,
                Announcement(_title.value, _body.value, _storageMetadata.value?.name.toString(), _downloadUri.value)
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
    private val courseRepository: CourseRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnnouncementsViewModel::class.java)) {
            return AnnouncementsViewModel(db, courseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}