package com.uniflow.app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class User(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("surname") @set:PropertyName("surname") var surname: String = "",
    @get:PropertyName("first_name") @set:PropertyName("first_name") var firstName: String = "",
    @get:PropertyName("last_name") @set:PropertyName("last_name") var lastName: String = "",
    @get:PropertyName("department") @set:PropertyName("department") var department: String = "",
    @get:PropertyName("department_id") @set:PropertyName("department_id") var departmentId: String = "",
    @get:PropertyName("position") @set:PropertyName("position") var position: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = UserRole.LECTURER.displayName,
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("password") @set:PropertyName("password") var password: String = "",
    @get:PropertyName("onboarded") @set:PropertyName("onboarded") var onboarded: Boolean = false,
    @get:PropertyName("must_change_password") @set:PropertyName("must_change_password") var mustChangePassword: Boolean = false,
    @get:PropertyName("password_hash") @set:PropertyName("password_hash") var passwordHash: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = ""
) {
    fun isAdmin() = role == UserRole.ADMIN.displayName
    fun isLecturer() = role == UserRole.LECTURER.displayName
}
