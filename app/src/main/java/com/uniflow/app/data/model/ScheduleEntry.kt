package com.uniflow.app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class ScheduleEntry(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("course_id") @set:PropertyName("course_id") var courseId: String = "",
    @get:PropertyName("lecturer_id") @set:PropertyName("lecturer_id") var lecturerId: String = "",
    @get:PropertyName("classroom_id") @set:PropertyName("classroom_id") var classroomId: String = "",
    @get:PropertyName("day") @set:PropertyName("day") var day: Int = 0,
    @get:PropertyName("time_slot") @set:PropertyName("time_slot") var timeSlot: Int = 0
)
