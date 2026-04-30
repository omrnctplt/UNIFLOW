package com.uniflow.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uniflow.app.data.model.Classroom
import com.uniflow.app.data.model.ScheduleEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveClassrooms(classrooms: List<Classroom>) {
        val batch = firestore.batch()
        classrooms.forEach { classroom ->
            val docRef = firestore.collection("classrooms").document(classroom.roomCode)
            batch.set(docRef, classroom)
        }
        batch.commit().await()
    }

    fun getAllClassrooms(): Flow<List<Classroom>> = callbackFlow {
        val subscription = firestore.collection("classrooms")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(Classroom::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createAssignment(entry: ScheduleEntry) {
        // Check for double booking
        val existing = firestore.collection("schedule_entries")
            .whereEqualTo("day", entry.day)
            .whereEqualTo("time_slot", entry.timeSlot)
            .get()
            .await()
            .toObjects(ScheduleEntry::class.java)

        val lecturerConflict = existing.any { it.lecturerId == entry.lecturerId }
        val classroomConflict = existing.any { it.classroomId == entry.classroomId }

        if (lecturerConflict) throw Exception("Hoca bu saatte zaten dolu.")
        if (classroomConflict) throw Exception("Derslik bu saatte zaten dolu.")

        firestore.collection("schedule_entries").document().set(entry).await()
    }

    fun getAllScheduleEntries(): Flow<List<ScheduleEntry>> = callbackFlow {
        val subscription = firestore.collection("schedule_entries")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(ScheduleEntry::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }
}
