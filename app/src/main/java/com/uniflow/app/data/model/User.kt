package com.uniflow.app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val name: String = "",
    val surname: String = "",
    val department: String = "",
    val position: String = "",
    val role: String = "Lecturer",
    val username: String = "",
    val password: String = "",
    val onboarded: Boolean = false // İsmi kısalttık, sorun çıkmasın
)
