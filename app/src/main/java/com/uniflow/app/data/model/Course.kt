package com.uniflow.app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Course(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("course_code") @set:PropertyName("course_code") var courseCode: String = "",
    @get:PropertyName("code") @set:PropertyName("code") var code: String = "", // Add for Phase 2 spec
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("lecturer_id") @set:PropertyName("lecturer_id") var lecturerId: String = "",
    @get:PropertyName("department") @set:PropertyName("department") var department: String = "", // Legacy
    @get:PropertyName("department_id") @set:PropertyName("department_id") var departmentId: String = "",
    @get:PropertyName("slot") @set:PropertyName("slot") var slot: Int = 0, // Legacy
    @get:PropertyName("day") @set:PropertyName("day") var day: Int = 0 // Legacy
)
