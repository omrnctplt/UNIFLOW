package com.uniflow.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test
import org.mockito.Mockito.mock
import org.junit.Assert.assertNull

class AuthRepositoryTest {

    private val auth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val firestore: FirebaseFirestore = mock(FirebaseFirestore::class.java)

    @Test
    fun `currentUser returns null when not logged in`() {
        val repo = AuthRepository(auth, firestore)
        assertNull(repo.currentUser)
    }
}
