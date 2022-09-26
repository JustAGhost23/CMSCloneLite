package com.example.cmsclonelite.repository

import com.example.cmsclonelite.Announcement
import com.example.cmsclonelite.Course
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CourseRepository {
    private suspend fun getCourses(db: FirebaseFirestore): List<DocumentSnapshot> {
        val coursesRef = db.collection("courses")
        val snapshot = coursesRef.get().await()
        return snapshot.documents
    }
    private suspend fun getUser(db: FirebaseFirestore, uid: String): DocumentSnapshot {
        val usersRef = db.collection("users").document(uid)
        return usersRef.get().await()
    }
    private suspend fun getCourse(db: FirebaseFirestore, courseId: String): DocumentSnapshot {
        val coursesRef = db.collection("courses").document(courseId)
        return coursesRef.get().await()
    }
    private suspend fun getUserEnrolledCourses(db: FirebaseFirestore, uid: String): List<DocumentSnapshot> {
        val snapshotList = arrayListOf<DocumentSnapshot>()
        val stringList = userEnrolledCourseList(db, uid)
        if (stringList != null) {
            for(string in stringList) {
                val coursesRef = db.collection("courses").document(string)
                snapshotList.add(coursesRef.get().await())
            }
        }
        return snapshotList
    }
    suspend fun getData(db: FirebaseFirestore): List<Course> {
        val docList = getCourses(db)
        val list = ArrayList<Course>()
        for (doc in docList) {
            val course = Course()
            course.id = doc.id
            course.courseName = doc.data?.get("name")?.toString()
            course.instructor = doc.data?.get("instructor")?.toString()
            course.days = doc.data?.get("days")?.toString()
            val timestampStartDateStartTime = doc.data?.get("startDateStartTime") as com.google.firebase.Timestamp
            course.startDateStartTime = timestampStartDateStartTime.toDate()
            val timestampStartDateEndTime = doc.data?.get("startDateEndTime") as com.google.firebase.Timestamp
            course.startDateEndTime = timestampStartDateEndTime.toDate()
            val timestampEndDateStartTime = doc.data?.get("endDateStartTime") as com.google.firebase.Timestamp
            course.endDateStartTime = timestampEndDateStartTime.toDate()
            val timestampEndDateEndTime = doc.data?.get("endDateEndTime") as com.google.firebase.Timestamp
            course.endDateEndTime = timestampEndDateEndTime.toDate()
            course.announcements = doc.data?.get("announcements") as HashMap<String, HashMap<String, String>>
            list.add(course)
        }
        return list
    }
    suspend fun getAnnouncements(db: FirebaseFirestore, courseId: String?): List<Announcement> {
        val list = ArrayList<Announcement>()
        if(courseId != null) {
            val doc = getCourse(db, courseId)
            val announcementMap: HashMap<String, HashMap<String, String>> = doc.data?.get("announcements") as HashMap<String, HashMap<String, String>>
            val announcementMapKeysList = announcementMap.keys.toList()
            for(i in announcementMapKeysList) {
                val announcement = Announcement()
                announcement.title = announcementMap[i]!!.getValue("title")
                announcement.body = announcementMap[i]!!.getValue("body")
                list.add(announcement)
            }
        }
        return list
    }
    suspend fun totalCourseCount(db: FirebaseFirestore): Int {
        val docList = getCourses(db)
        return docList.size
    }
    suspend fun userTotalEnrolledCourseCount(db: FirebaseFirestore, uid: String): Int {
        val doc = getUser(db, uid)
        val enrolledCourseList: List<String>? = doc.get("enrolled") as List<String>?
        return enrolledCourseList?.size ?: 0
    }
    suspend fun userEnrolledCourseList(db: FirebaseFirestore, uid: String): List<String>? {
        val doc = getUser(db, uid)
        return doc.get("enrolled") as List<String>?
    }
    suspend fun getUserEnrolledCoursesData(db: FirebaseFirestore, uid: String): List<Course> {
        val docList = getUserEnrolledCourses(db, uid)
        val courseList = ArrayList<Course>()
        for(doc in docList) {
            val course = Course()
            course.id = doc.id
            course.courseName = doc.data?.get("name")?.toString()
            course.instructor = doc.data?.get("instructor")?.toString()
            course.days = doc.data?.get("days")?.toString()
            val timestampStartDateStartTime = doc.data?.get("startDateStartTime") as com.google.firebase.Timestamp
            course.startDateStartTime = timestampStartDateStartTime.toDate()
            val timestampStartDateEndTime = doc.data?.get("startDateEndTime") as com.google.firebase.Timestamp
            course.startDateEndTime = timestampStartDateEndTime.toDate()
            val timestampEndDateStartTime = doc.data?.get("endDateStartTime") as com.google.firebase.Timestamp
            course.endDateStartTime = timestampEndDateStartTime.toDate()
            val timestampEndDateEndTime = doc.data?.get("endDateEndTime") as com.google.firebase.Timestamp
            course.endDateEndTime = timestampEndDateEndTime.toDate()
            course.announcements = doc.data?.get("announcements") as HashMap<String, HashMap<String, String>>
            courseList.add(course)
        }
        return courseList
    }
    fun addAnnouncement(db: FirebaseFirestore, courseId: String, announcement: Announcement) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            val announcementList = getAnnouncements(db, courseId)
            val newAnnouncementList: ArrayList<Announcement> = arrayListOf()
            val announcementHashMap: HashMap<String, HashMap<String, String>> = hashMapOf()
            for(i in announcementList) {
                newAnnouncementList.add(i)
            }
            newAnnouncementList.reverse()
            newAnnouncementList.add(announcement)
            var count = newAnnouncementList.size
            for(i in newAnnouncementList) {
                announcementHashMap["key${count}"] = hashMapOf(
                    "title" to i.title!!,
                    "body" to i.body!!
                )
                count -= 1
            }
            db.collection("courses").document(courseId)
                .update("announcements", announcementHashMap)
        }
    }
}