package com.uniflow.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.uniflow.app.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun login(username: String, pass: String) {
        val email = "$username@uniflow.com"
        auth.signInWithEmailAndPassword(email, pass).await()
    }

    suspend fun register(user: User, pass: String) {
        val email = "${user.username}@uniflow.com"
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")
        val newUser = user.copy(uid = uid)
        firestore.collection("users").document(uid).set(newUser).await()
    }

    suspend fun getUserData(uid: String): User? {
        return firestore.collection("users").document(uid).get().await().toObject(User::class.java)
    }

    suspend fun findImportedUser(username: String): User? {
        Log.d("UniFlowAuth", "Searching for imported user: $username")
        val doc = firestore.collection("users").document(username).get().await()
        return if (doc.exists()) {
            Log.d("UniFlowAuth", "User found in Firestore!")
            doc.toObject(User::class.java)
        } else {
            Log.d("UniFlowAuth", "User NOT found in Firestore.")
            null
        }
    }

    suspend fun upgradeImportedUser(user: User, pass: String) {
        Log.d("UniFlowAuth", "Upgrading user to Firebase Auth: ${user.username}")
        val email = "${user.username}@uniflow.com"
        
        // Önce Firebase Auth hesabı oluştur
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        val uid = result.user?.uid ?: throw Exception("Auth creation failed")
        
        // Veriyi UID ile yeni dökümana taşı
        val updatedUser = user.copy(uid = uid)
        firestore.collection("users").document(uid).set(updatedUser).await()
        
        // Eski (username bazlı) dökümanı sil
        firestore.collection("users").document(user.username).delete().await()
        Log.d("UniFlowAuth", "Upgrade complete for UID: $uid")
    }

    suspend fun setOnboarded(uid: String) {
        firestore.collection("users").document(uid).update("onboarded", true).await()
    }

    suspend fun changePassword(newPass: String) {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        user.updatePassword(newPass).await()
    }

    suspend fun setMustChangePassword(uid: String, mustChange: Boolean) {
        firestore.collection("users").document(uid).update("must_change_password", mustChange).await()
    }

    fun logout() = auth.signOut()
}
