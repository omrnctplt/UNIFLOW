package com.uniflow.app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Lecturer(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("first_name") @set:PropertyName("first_name") var firstName: String = "",
    @get:PropertyName("last_name") @set:PropertyName("last_name") var lastName: String = "",
    @get:PropertyName("department_id") @set:PropertyName("department_id") var departmentId: String = "",
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("password_hash") @set:PropertyName("password_hash") var passwordHash: String = "",
    @get:PropertyName("must_change_password") @set:PropertyName("must_change_password") var mustChangePassword: Boolean = false,
    @get:PropertyName("role") @set:PropertyName("role") var role: String = UserRole.LECTURER.displayName
)
