package com.uniflow.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test
import org.mockito.Mockito.mock

class DataRepositoryTest {

    private val firestore: FirebaseFirestore = mock(FirebaseFirestore::class.java)

    @Test
    fun `DataRepository instantiates successfully`() {
        val repo = DataRepository(firestore)
        assert(repo != null)
    }
}
