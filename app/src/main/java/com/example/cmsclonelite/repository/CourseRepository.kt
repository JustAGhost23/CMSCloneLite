package com.example.cmsclonelite.repository

import com.example.cmsclonelite.Announcement
import com.example.cmsclonelite.Course
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class CourseRepository {
    private suspend fun getCourses(db: FirebaseFirestore): List<DocumentSnapshot> {
        val coursesRef = db.collection("courses")
        val snapshot = coursesRef.get().await()
        return snapshot.documents
    }
    private suspend fun getUser(db: FirebaseFirestore, uid: String): DocumentSnapshot {
        val coursesRef = db.collection("users").document(uid)
        return coursesRef.get().await()
    }
    private suspend fun getCourse(db: FirebaseFirestore, courseId: String): DocumentSnapshot {
        val coursesRef = db.collection("courses").document(courseId)
        return coursesRef.get().await()
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
            val timestampStartDate = doc.data?.get("startdate") as com.google.firebase.Timestamp
            course.startDate = timestampStartDate.toDate()
            val timestampEndDate = doc.data?.get("enddate") as com.google.firebase.Timestamp
            course.endDate = timestampEndDate.toDate()
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
}