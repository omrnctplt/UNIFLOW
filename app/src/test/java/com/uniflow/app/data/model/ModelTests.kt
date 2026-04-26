package com.uniflow.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelTests {
    @Test
    fun `test User model default values`() {
        val user = User()
        assertEquals("", user.uid)
        assertEquals("", user.username)
        assertEquals("Lecturer", user.role)
        assertEquals(false, user.mustChangePassword)
        assertEquals(false, user.onboarded)
    }

    @Test
    fun `test Classroom model default values`() {
        val classroom = Classroom()
        assertEquals("", classroom.id)
        assertEquals("", classroom.roomCode)
        assertEquals(0, classroom.capacity)
        assertEquals("", classroom.departmentId)
    }
}
