package com.uniflow.app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class User(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("surname") @set:PropertyName("surname") var surname: String = "",
    @get:PropertyName("department") @set:PropertyName("department") var department: String = "", // Legacy
    @get:PropertyName("department_id") @set:PropertyName("department_id") var departmentId: String = "",
    @get:PropertyName("position") @set:PropertyName("position") var position: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "Lecturer",
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("password") @set:PropertyName("password") var password: String = "", // Legacy
    @get:PropertyName("onboarded") @set:PropertyName("onboarded") var onboarded: Boolean = false,
    @get:PropertyName("must_change_password") @set:PropertyName("must_change_password") var mustChangePassword: Boolean = false,
    @get:PropertyName("password_hash") @set:PropertyName("password_hash") var passwordHash: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = ""
)
