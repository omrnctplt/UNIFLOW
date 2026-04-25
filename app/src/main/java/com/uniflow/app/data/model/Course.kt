package com.uniflow.app.data.model

data class Course(
    val courseCode: String = "",
    val name: String = "",
    val lecturerId: String = "",
    val department: String = "",
    val slot: Int = 0, // 0-9 for 5x2 grid (0: Mon Morning, 1: Mon Afternoon, etc.)
    val day: Int = 0 // 0-4
)
