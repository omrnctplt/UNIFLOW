package com.uniflow.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uniflow.app.data.model.Classroom
import com.uniflow.app.data.model.Course
import com.uniflow.app.data.model.ScheduleEntry
import com.uniflow.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveImportedData(courses: List<Course>, lecturers: List<User>) {
        val allOps = mutableListOf<suspend (com.google.firebase.firestore.WriteBatch) -> Unit>()
        
        courses.forEach { course ->
            allOps.add { batch ->
                val docRef = firestore.collection("courses").document(course.courseCode)
                batch.set(docRef, course)
            }
        }
        
        lecturers.forEach { lecturer ->
            allOps.add { batch ->
                val docRef = firestore.collection("users").document(lecturer.username)
                batch.set(docRef, lecturer)
            }
        }

        // Execute in chunks of 500 with a safety timeout
        allOps.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { op -> op(batch) }
            
            // If Firestore doesn't respond in 15 seconds, throw exception to break the loading loop
            withTimeout(15000) {
                batch.commit().await()
            }
        }
    }

    fun getCoursesCount(): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("courses")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.size() ?: 0)
            }
        awaitClose { subscription.remove() }
    }

    fun getLecturersCount(): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("users")
            .whereEqualTo("role", "Lecturer")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.size() ?: 0)
            }
        awaitClose { subscription.remove() }
    }

    fun getAllCourses(): Flow<List<Course>> = callbackFlow {
        val subscription = firestore.collection("courses")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.toObjects(Course::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    fun getAllLecturers(): Flow<List<User>> = callbackFlow {
        val subscription = firestore.collection("users")
            .whereEqualTo("role", "Lecturer")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.toObjects(User::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getCoursesByLecturer(lecturerId: String): List<Course> {
        return firestore.collection("courses")
            .whereEqualTo("lecturerId", lecturerId)
            .get()
            .await()
            .toObjects(Course::class.java)
    }

    // Phase 2 Methods

    fun getAllClassrooms(): Flow<List<Classroom>> = callbackFlow {
        val subscription = firestore.collection("classrooms")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.toObjects(Classroom::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addClassroom(classroom: Classroom) {
        val docRef = firestore.collection("classrooms").document()
        classroom.id = docRef.id
        docRef.set(classroom).await()
    }

    fun getAllScheduleEntries(): Flow<List<ScheduleEntry>> = callbackFlow {
        val subscription = firestore.collection("schedule_entries")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.toObjects(ScheduleEntry::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    fun getScheduleEntriesForLecturer(lecturerId: String): Flow<List<ScheduleEntry>> = callbackFlow {
        val subscription = firestore.collection("schedule_entries")
            .whereEqualTo("lecturer_id", lecturerId)
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.toObjects(ScheduleEntry::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addScheduleEntry(entry: ScheduleEntry) {
        // Prevent double booking logic
        val existingEntries = firestore.collection("schedule_entries")
            .whereEqualTo("day", entry.day)
            .whereEqualTo("time_slot", entry.timeSlot)
            .get()
            .await()
            .toObjects(ScheduleEntry::class.java)

        for (existing in existingEntries) {
            if (existing.classroomId == entry.classroomId) {
                throw Exception("Classroom is already booked for this time slot.")
            }
            if (existing.lecturerId == entry.lecturerId) {
                throw Exception("Lecturer is already booked for this time slot.")
            }
        }

        val docRef = firestore.collection("schedule_entries").document()
        entry.id = docRef.id
        docRef.set(entry).await()
    }
}
