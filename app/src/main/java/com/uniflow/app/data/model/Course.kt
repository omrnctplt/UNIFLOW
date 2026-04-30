package com.uniflow.app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Course(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("code") @set:PropertyName("code") var code: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("department_id") @set:PropertyName("department_id") var departmentId: String = "",
    @get:PropertyName("lecturer_id") @set:PropertyName("lecturer_id") var lecturerId: String = ""
)
