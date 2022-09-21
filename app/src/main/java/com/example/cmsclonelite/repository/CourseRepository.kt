package com.example.cmsclonelite.repository

import com.example.cmsclonelite.Course
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CourseRepository {
    private suspend fun getCourses(db: FirebaseFirestore): List<DocumentSnapshot> {
        val coursesRef = db.collection("courses")
        val snapshot = coursesRef.get().await()
        return snapshot.documents
    }
    suspend fun readData(db: FirebaseFirestore): List<Course> {
        val docList = getCourses(db)
        val list = ArrayList<Course>()
        for (document in docList) {
            val course = Course()
            course.id = document.id
            course.courseName = document.data?.get("name")?.toString()
            course.instructor = document.data?.get("instructor")?.toString()
            course.days = document.data?.get("days")?.toString()
            val timestampStartDate = document.data?.get("startdate") as com.google.firebase.Timestamp
            course.startDate = timestampStartDate.toDate()
            val timestampEndDate = document.data?.get("enddate") as com.google.firebase.Timestamp
            course.endDate = timestampEndDate.toDate()
            course.announcements = document.data?.get("announcements") as HashMap<String, HashMap<String, String>>
            list.add(course)
        }
        return list
    }
    suspend fun totalCourseCount(db: FirebaseFirestore): Int {
        val docList = getCourses(db)
        return docList.size
    }
}