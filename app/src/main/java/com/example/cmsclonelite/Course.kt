package com.example.cmsclonelite

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*
import kotlin.collections.HashMap

@Parcelize
data class Course (
    var id: String? = null,
    var courseName: String? = null,
    var instructor: String? = null,
    var days: String? = null,
    var startDateStartTime: @RawValue Date? = null,
    var startDateEndTime: @RawValue Date? = null,
    var endDateStartTime: @RawValue Date? = null,
    var endDateEndTime: @RawValue Date? = null,
    var announcements: HashMap<String, HashMap<String, String>> = hashMapOf()
): Parcelable

@Parcelize
data class Announcement (
    var title: String? = null,
    var body: String? = null
): Parcelable