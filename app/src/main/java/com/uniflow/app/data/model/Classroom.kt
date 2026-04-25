package com.uniflow.app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Classroom(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("room_code") @set:PropertyName("room_code") var roomCode: String = "",
    @get:PropertyName("capacity") @set:PropertyName("capacity") var capacity: Int = 0,
    @get:PropertyName("department_id") @set:PropertyName("department_id") var departmentId: String = ""
)
