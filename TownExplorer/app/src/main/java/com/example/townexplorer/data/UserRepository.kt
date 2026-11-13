package com.example.townexplorer.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * A document in the Firestore `users` collection (document id = the user's uid).
 */
data class UserProfile(
    val email: String = "",
    val displayName: String? = null,
    val createdAt: Timestamp? = null
)

/**
 * Reads and writes user profiles in Firestore.
 *   Source: https://firebase.google.com/docs/firestore/manage-data/add-data
 */
object UserRepository {

    private const val TAG = "UserRepository"

    private val users: CollectionReference
        get() = Firebase.firestore.collection("users")

    suspend fun createProfile(uid: String, email: String) {
        val profile = mapOf(
            "email" to email,
            "displayName" to null,
            "createdAt" to FieldValue.serverTimestamp()
        )
        users.document(uid).set(profile).await()
    }

    // Returns null if the profile does not exist or could not be loaded.
    suspend fun getProfile(uid: String): UserProfile? {
        return try {
            users.document(uid).get().await().toObject(UserProfile::class.java)
        } catch (e: FirebaseFirestoreException) {
            Log.w(TAG, "Could not load the user profile", e)
            null
        }
    }
}
