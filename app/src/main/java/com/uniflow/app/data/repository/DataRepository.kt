package com.uniflow.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uniflow.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

class ScheduleConflictException(message: String) : Exception(message)

@Singleton
class DataRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // --- Data Import & Sync ---

    suspend fun saveImportedData(courses: List<Course>, lecturers: List<Lecturer>) {
        val allOps = mutableListOf<suspend (com.google.firebase.firestore.WriteBatch) -> Unit>()
        
        courses.forEach { course ->
            allOps.add { batch ->
                val docRef = firestore.collection("courses").document(course.id.ifEmpty { course.code })
                batch.set(docRef, course)
            }
        }
        
        lecturers.forEach { lecturer ->
            allOps.add { batch ->
                val docRef = firestore.collection("lecturers").document(lecturer.id.ifEmpty { lecturer.username })
                batch.set(docRef, lecturer)
            }
        }

        allOps.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { op -> op(batch) }
            withTimeout(15000) {
                batch.commit().await()
            }
        }
    }

    suspend fun addLecturersBatch(lecturers: List<Lecturer>) {
        val batch = firestore.batch()
        lecturers.forEach { lecturer ->
            val docRef = firestore.collection("lecturers").document()
            val lecturerWithId = if (lecturer.id.isEmpty()) lecturer.copy(id = docRef.id) else lecturer
            batch.set(docRef, lecturerWithId)
        }
        batch.commit().await()
    }

    suspend fun addCoursesBatch(courses: List<Course>) {
        val batch = firestore.batch()
        courses.forEach { course ->
            val docRef = firestore.collection("courses").document()
            val courseWithId = if (course.id.isEmpty()) course.copy(id = docRef.id) else course
            batch.set(docRef, courseWithId)
        }
        batch.commit().await()
    }

    // --- Departments ---

    fun getAllDepartments(): Flow<List<Department>> = callbackFlow {
        val subscription = firestore.collection("departments")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.toObjects(Department::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addDepartment(department: Department) {
        val docRef = firestore.collection("departments").document()
        department.id = docRef.id
        docRef.set(department).await()
    }

    // --- Courses & Lecturers ---

    fun getCoursesCount(): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("courses")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.size() ?: 0)
            }
        awaitClose { subscription.remove() }
    }

    fun getLecturersCount(): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("lecturers")
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

    fun getAllLecturers(): Flow<List<Lecturer>> = callbackFlow {
        val subscription = firestore.collection("lecturers")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.toObjects(Lecturer::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun findLecturerByUsername(username: String): Lecturer? {
        return firestore.collection("lecturers")
            .whereEqualTo("username", username)
            .get()
            .await()
            .toObjects(Lecturer::class.java)
            .firstOrNull()
    }

    suspend fun updateLecturer(lecturer: Lecturer) {
        firestore.collection("lecturers").document(lecturer.id).set(lecturer).await()
    }

    // --- Classrooms ---

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

    // --- Schedule & Assignments ---

    fun getAllScheduleEntries(): Flow<List<ScheduleEntry>> = callbackFlow {
        val subscription = firestore.collection("schedule_entries")
            .addSnapshotListener { snapshot, error ->
                if (error == null) trySend(snapshot?.toObjects(ScheduleEntry::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addScheduleEntry(entry: ScheduleEntry) {
        val existingEntries = firestore.collection("schedule_entries")
            .whereEqualTo("day", entry.day)
            .whereEqualTo("time_slot", entry.timeSlot)
            .get()
            .await()
            .toObjects(ScheduleEntry::class.java)

        for (existing in existingEntries) {
            if (existing.classroomId == entry.classroomId) {
                throw ScheduleConflictException("Bu derslik belirtilen gün ve saatte zaten dolu.")
            }
            if (existing.lecturerId == entry.lecturerId) {
                throw ScheduleConflictException("Bu hoca belirtilen gün ve saatte zaten dolu.")
            }
        }

        val docRef = firestore.collection("schedule_entries").document()
        entry.id = docRef.id
        docRef.set(entry).await()
    }

    suspend fun deleteScheduleEntry(id: String) {
        firestore.collection("schedule_entries").document(id).delete().await()
    }
}
