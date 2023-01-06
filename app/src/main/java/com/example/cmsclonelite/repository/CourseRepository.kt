package com.example.cmsclonelite.repository

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import com.example.cmsclonelite.Announcement
import com.example.cmsclonelite.Course
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CourseRepository {
    private val client = OkHttpClient()
    private val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val storage = Firebase.storage
    var storageRef = storage.reference

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
                announcement.fileName = announcementMap[i]!!.getValue("fileName")
                announcement.downloadUri = announcementMap[i]!!.getValue("downloadUri").toUri()
                list.add(announcement)
            }
        }
        return list
    }

    suspend fun unenrollAll(db: FirebaseFirestore, uid: String) {
        withContext(Dispatchers.IO) {
                db.collection("users").document(uid)
                    .update("enrolled", listOf<String>())
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                    }
                    .addOnFailureListener { e: Exception? ->
                        Log.w(TAG, "Error updating document", e)
                    }
        }
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
            val timestampEndDateEndTime = doc.data?.get("endDateEndTime") as com.google.firebase.Timestamp
            course.endDateEndTime = timestampEndDateEndTime.toDate()
            course.announcements = doc.data?.get("announcements") as HashMap<String, HashMap<String, String>>
            courseList.add(course)
        }
        return courseList
    }

    fun calendarExport(context: Context, email: String, course: Course) {
        ioCoroutineScope.launch {
            val calID: Long? = getCalendarId(context, email)
            val startMillis: Long = Calendar.getInstance().run {
                set(
                    course.startDateStartTime!!.year + 1900,
                    course.startDateStartTime!!.month,
                    course.startDateStartTime!!.date,
                    course.startDateStartTime!!.hours,
                    course.startDateStartTime!!.minutes
                )
                timeInMillis
            }
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.TITLE, course.courseName)
                put(CalendarContract.Events.CALENDAR_ID, calID)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(
                    CalendarContract.Events.DURATION,
                    "PT${course.endDateEndTime!!.hours - course.startDateStartTime!!.hours}H${course.endDateEndTime!!.minutes - course.startDateStartTime!!.minutes}M"
                )
                put(
                    CalendarContract.Events.RRULE,
                    "FREQ=WEEKLY;UNTIL=${course.endDateEndTime!!.year + 1900}${
                        (course.endDateEndTime!!.month + 1).toString()
                            .padStart(2, '0')
                    }${
                        course.endDateEndTime!!.date.toString().padStart(2, '0')
                    }T${
                        course.endDateEndTime!!.hours.toString().padStart(2, '0')
                    }${
                        course.endDateEndTime!!.minutes.toString().padStart(2, '0')
                    }${
                        course.endDateEndTime!!.seconds.toString().padStart(2, '0')
                    }Z;BYDAY=${
                        course.days.toString().substring(0, (course.days.toString().length - 1))
                    }"
                );
            }
            context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI,
                values
            )!!
        }
    }

    private fun getCalendarId(context: Context, email: String) : Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

        var calCursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.ACCOUNT_NAME + " = '$email'",
            null,
            CalendarContract.Calendars._ID + " ASC"
        )

        if (calCursor != null && calCursor.count <= 0) {
            calCursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.ACCOUNT_NAME + " = '$email'",
                null,
                CalendarContract.Calendars._ID + " ASC"
            )
        }

        if (calCursor != null) {
            if (calCursor.moveToFirst()) {
                val calName: String
                val calID: String
                val nameCol = calCursor.getColumnIndex(projection[1])
                val idCol = calCursor.getColumnIndex(projection[0])

                calName = calCursor.getString(nameCol)
                calID = calCursor.getString(idCol)

                Log.d(TAG, "Calendar name = $calName Calendar ID = $calID")

                calCursor.close()
                return calID.toLong()
            }
        }
        return null
    }

    fun enrollInCourse(db: FirebaseFirestore, uid: String, course: Course) {
        ioCoroutineScope.launch {
            FirebaseMessaging.getInstance().subscribeToTopic(course.id!!)
            db.collection("users").document(uid)
                .update("enrolled", FieldValue.arrayUnion(course.id))
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e: Exception? -> Log.w(ContentValues.TAG, "Error updating document", e) }
        }
    }

    fun unenrollFromCourse(db: FirebaseFirestore, uid:String, course: Course) {
        ioCoroutineScope.launch {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(course.id!!)
            db.collection("users").document(uid)
                .update("enrolled", FieldValue.arrayRemove("${course.id}"))
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e: Exception? -> Log.w(TAG, "Error updating document", e) }
        }
    }

    fun deleteCourse(db: FirebaseFirestore, course: Course) {
        ioCoroutineScope.launch {
            val fileRef = storageRef.child("files/${course.id}")
            fileRef.listAll().addOnCompleteListener { dir ->
                for (item in dir.result.items) {
                    item.delete()
                }
            }
            db.collection("users").whereArrayContains("enrolled", course.id!!)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        val documents = task.result
                        for(document in documents) {
                            val user = document.id
                            val username = document.get("name")
                            val courseList: MutableList<String> = document.get("enrolled") as MutableList<String>
                            courseList.remove(course.id)
                            val userInfo = hashMapOf(
                                "name" to username,
                                "enrolled" to courseList
                            )
                            db.collection("users").document(user)
                                .set(userInfo)
                        }
                    }
                }
            db.collection("courses").document("${course.id}")
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e: Exception? -> Log.w(TAG, "Error deleting document", e) }
        }
    }

    fun addCourse(db: FirebaseFirestore, course: Course) {
        ioCoroutineScope.launch {
            val data = hashMapOf(
                "name" to course.courseName,
                "instructor" to course.instructor,
                "days" to course.days,
                "startDateStartTime" to com.google.firebase.Timestamp(course.startDateStartTime!!),
                "endDateEndTime" to com.google.firebase.Timestamp(course.endDateEndTime!!),
                "announcements" to hashMapOf<String, HashMap<String, String>>()
            )
            db.collection("courses")
                .add(data)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }
    }

    fun editCourse(db: FirebaseFirestore, courseId: String, course: Course) {
        ioCoroutineScope.launch {
            val data = hashMapOf(
                "name" to course.courseName,
                "instructor" to course.instructor,
                "days" to course.days,
                "startDateStartTime" to com.google.firebase.Timestamp(course.startDateStartTime!!),
                "endDateEndTime" to com.google.firebase.Timestamp(course.endDateEndTime!!),
                "announcements" to hashMapOf<String, HashMap<String, String>>()
            )
            db.collection("courses").document(courseId)
                .set(data, SetOptions.merge())
        }
    }

    fun addAnnouncement(db: FirebaseFirestore, courseId: String, announcement: Announcement) {
        ioCoroutineScope.launch {
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
                    "body" to i.body!!,
                    "fileName" to i.fileName,
                    "downloadUri" to i.downloadUri.toString()
                )
                count -= 1
            }
            db.collection("courses").document(courseId)
                .update("announcements", announcementHashMap)
        }
    }

    suspend fun uploadFileToFirebase(course: Course, fileUri: Uri, context: Context): Uri? {
        val returnCursor = context.contentResolver.query(fileUri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val fileName = nameIndex?.let { returnCursor.getString(it) }
        returnCursor?.close()
        val fileRef = storageRef.child("files/${course.id}/${fileName}")
        fileRef.putFile(fileUri).await()
        return fileRef.downloadUrl.await()
    }

    suspend fun getFileMetadataFromFirebase(course: Course, fileUri: Uri, context: Context): StorageMetadata {
        val returnCursor = context.contentResolver.query(fileUri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val fileName = nameIndex?.let { returnCursor.getString(it) }
        returnCursor?.close()
        val fileRef = storageRef.child("files/${course.id}/${fileName}")
        return fileRef.metadata.await()
    }

    suspend fun deleteFileFromFirebase(course: Course, fileUri: Uri, context: Context) {
        val returnCursor = context.contentResolver.query(fileUri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val fileName = nameIndex?.let { returnCursor.getString(it) }
        returnCursor?.close()
        val fileRef = storageRef.child("files/${course.id}/${fileName}")
        fileRef.delete().await()
    }

    fun sendPushNotification(course: Course, announcement: Announcement) {
        ioCoroutineScope.launch {
            val url = "https://fcm.googleapis.com/v1/projects/cmsclonelite/messages:send"
            val keyFunc = getAccessToken()
            val key = keyFunc.await()
            val bodyJson = JSONObject()
            bodyJson.put("message",  JSONObject().also { jsonObject ->

            jsonObject.put("topic", course.id)
            jsonObject.put("notification",
                JSONObject().also {
                    it.put("title", course.courseName)
                    it.put("body", "A new announcement with title \"${announcement.title}\" was created")
                }
            )
        })

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${key.tokenValue}")
            .post(
                bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    println("Received data: ${response.body?.string()}")
                }
                override fun onFailure(call: Call, e: IOException) {
                    println(e.message.toString())
                }
            }
        )
        }
    }

    @Throws(IOException::class)
    private fun getAccessToken(): Deferred<AccessToken> = ioCoroutineScope.async {
        val myString: String = """
            {
            "type": "service_account",
            "project_id": "cmsclonelite",
            "private_key_id": "deb8cf6e9e8662b73d25b6cc9de50ffd18fc6408",
            "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCbKyYXafM5Seau\nN1dmMQnKLZTMwdLggQgELvqwripsr9qwPoYkc9iiXsmbmpTPFSIwaHw+0KFrIDEj\nBuY7m5kjuOr8QuwYr8uF0+APorioIF4Alh5uaJwy8N8SnxEtEqYEVQHD0nRZrZAq\n7aH+AkgjCSZb1qMeTi0qodr0Yo4Leo1xfj+PsaXlwHc4Lxzk1YX5eIyhKPSIzDAN\nR7Rf63TBJqoyhS7W+OnFhNyFzBY7tB7MVO/deTC7a8Fq1j0jcxP4WgtoqGRK8Jrw\nm5qgFXPVcLZejCa9HkfHNDCUQX97LieuwaNvutukCGo+Ju0gAY55Qhbcm1qmMr2M\nOy9BqwETAgMBAAECggEAK77v98Tmjf2wO+idStHDdU0BePKiDapDBM7r3VjU3Bx9\n9pdCAU/pwOkhRK3Bi1plb6ldceYmxAlo4PumeCKne4M8OI9zeYhCREIWFsJk9f1e\nEyM/hBgZBWGUOm8/C+qIhIskur0AEXJVxUyGkZK6HKqNNnIePcKDiFPmY2TPsBtV\n20mIft0KSwM0RW+xfbMc3PA71iHK2e9M1RqF+OPh/3Fmhp9+1T67+kCxOjAl/Oi5\ncHUJuixZvhGEsHimpLUeo5lKIEv7+a91FaDQ2nCR3XmrUvv/eKG2SisQxK/WECfl\nzOHP/jvDJWMbMCaBkpgLsC4kIoqUrD4+ujzAdqgpuQKBgQDUw5s7wWNTHgUwuVFI\nhfGnAB83TDdoWFwVeng5QmUWM4lyiytLoULoh71EdOUNkRVCAeBaswdisA5LGxm4\naCgp9pO+1de1qQaMxh0SEquXuHh2F3Xw/JrMeB/UnltQ5dB2RPRNPFadM09T34MA\n6ntQHD8Bd+E5zmFFVI8hehKACwKBgQC6s1ANoX1zUafRgx0bvdN+NPl4femL3Eci\n8YkBEibNkoGqkgU3smp4BjB1i/7z7JHKvdqsheyLX4a9qQJa8Er/1UkpHcGHCAJO\nN+94JqRRkKmDbtARQwzRX6VNRTE/TNcaFlXbKCpGQLJRZbCnGovUku0JAtejLTlr\noUrZJOWAGQKBgGLhjbGVzQ2B9DOSzN4Bsi9E9T0D5PRPrGwnANzLqKNKzGQ5naOG\njMv42dOI6DMH7HNC3/wHHDWXUO1C6Q81CQWHnV6hj6DTr0GZiUUu/CSFDScE/EFF\nhMPmSBTOi+3rAJkWrtt+YVqp0AJQ7FduomS22+lYx4nQCpDoHaUXUKXFAoGBAJzT\nRmlMv14AtyDMK8VDvMWEphFKYsUysSZvERAvORzw9a5bnbpdSgWr3US/5dbrXsOY\nmnjUvg+MnFfwAaR1t+oSNLQu5IMSfS1K2wJoIxrIkztt96SoV7n/x5CSkH2FhCHL\npS7EHE6Kxb6N1sdnCxHyoN0y4AOXV2ZLQ94GmijxAoGAEk6xVETYjR6FR8dWZaa+\nsdysAtnDr8fZ9ckbvHqKJ6ji5aNxM8Di9uzTQ/zZnMwNqbKsJLO6rfQhgHmqVZR4\n9L12dDmkWXwnmLv9IfrxOT8zNBdPHzrjH5xx3lZ1KyTQaUcVtztxGUSWjJ96DXQ8\nBXlWvIERFWERO3FOBhD99As=\n-----END PRIVATE KEY-----\n",
            "client_email": "firebase-adminsdk-dm3qc@cmsclonelite.iam.gserviceaccount.com",
            "client_id": "106259377800798371057",
            "auth_uri": "https://accounts.google.com/o/oauth2/auth",
            "token_uri": "https://oauth2.googleapis.com/token",
            "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
            "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-dm3qc%40cmsclonelite.iam.gserviceaccount.com"
            }
            """.trimIndent()
        val inputStream: InputStream = myString.byteInputStream()
        val googleCredentials: GoogleCredentials = GoogleCredentials
            .fromStream(inputStream)
            .createScoped(
                listOf(
                    "https://www.googleapis.com/auth/firebase",
                    "https://www.googleapis.com/auth/cloud-platform",
                    "https://www.googleapis.com/auth/firebase.readonly"
                )
            )
        googleCredentials.refreshAccessToken()
        googleCredentials.refresh()
        googleCredentials.accessToken
    }
}