package com.uniflow.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.uniflow.app.data.model.User
import com.uniflow.app.utils.HashUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun loginCustom(username: String, passwordHash: String): User? {
        Log.d("AuthRepository", "Attempting login for user: $username")
        
        // Try searching in both collections
        val collections = listOf("users", "lecturers")
        
        for (col in collections) {
            // 1. Try by document ID
            val docById = firestore.collection(col).document(username).get().await()
            if (docById.exists()) {
                val data = docById.data
                val storedHash = data?.get("password_hash") as? String
                if (storedHash == passwordHash) {
                    return mapToUser(data, username)
                }
            }
            
            // 2. Try by field query
            val query = firestore.collection(col).whereEqualTo("username", username).get().await()
            if (!query.isEmpty) {
                val doc = query.documents.first()
                val data = doc.data
                val storedHash = data?.get("password_hash") as? String
                if (storedHash == passwordHash) {
                    return mapToUser(data, username)
                }
            }
        }
        
        Log.d("AuthRepository", "Login failed: User not found or password mismatch for $username")
        return null
    }

    private fun mapToUser(data: Map<String, Any>?, username: String): User {
        if (data == null) return User(username = username)
        
        // Manual mapping to handle different field names (camelCase vs snake_case)
        return User(
            username = data["username"] as? String ?: username,
            passwordHash = data["password_hash"] as? String ?: "",
            role = data["role"] as? String ?: "Lecturer",
            firstName = (data["first_name"] ?: data["firstName"]) as? String ?: "",
            lastName = (data["last_name"] ?: data["lastName"]) as? String ?: "",
            name = data["name"] as? String ?: "",
            surname = data["surname"] as? String ?: "",
            departmentId = (data["department_id"] ?: data["departmentId"]) as? String ?: "",
            mustChangePassword = data["must_change_password"] as? Boolean ?: false,
            onboarded = data["onboarded"] as? Boolean ?: false,
            id = (data["id"] ?: data["uid"]) as? String ?: username
        )
    }

    suspend fun updatePassword(username: String, newPasswordHash: String) {
        val collections = listOf("users", "lecturers")
        for (col in collections) {
            val docById = firestore.collection(col).document(username).get().await()
            if (docById.exists()) {
                firestore.collection(col).document(username).update(
                    "password_hash", newPasswordHash,
                    "must_change_password", false
                ).await()
                return
            }
            
            val query = firestore.collection(col).whereEqualTo("username", username).get().await()
            if (!query.isEmpty) {
                query.documents.first().reference.update(
                    "password_hash", newPasswordHash,
                    "must_change_password", false
                ).await()
                return
            }
        }
    }

    suspend fun register(user: User, passHash: String) {
        val newUser = user.copy(passwordHash = passHash)
        firestore.collection("users").document(user.username).set(newUser).await()
    }

    suspend fun getUserData(username: String): User? {
        val collections = listOf("users", "lecturers")
        for (col in collections) {
            val doc = firestore.collection(col).document(username).get().await()
            if (doc.exists()) return mapToUser(doc.data, username)
            
            val query = firestore.collection(col).whereEqualTo("username", username).get().await()
            if (!query.isEmpty) return mapToUser(query.documents.first().data, username)
        }
        return null
    }

    fun logout() {
        // Local session clear
    }
}
