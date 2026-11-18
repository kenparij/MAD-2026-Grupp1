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

    /**
     * Fetches all visited tiles for a user from Firestore (users/{uid}/visited_tiles).
     */
    suspend fun getVisitedTiles(uid: String): Set<Pair<Int, Int>> {
        return try {
            Log.d(TAG, "Fetching visited tiles from Firestore for user: $uid")
            val snapshot = users.document(uid)
                .collection("visited_tiles")
                .get()
                .await()

            val tiles = snapshot.documents.mapNotNull { doc ->
                val x = doc.getLong("x")?.toInt()
                val y = doc.getLong("y")?.toInt()
                if (x != null && y != null) Pair(x, y) else null
            }.toSet()
            Log.d(TAG, "Loaded ${tiles.size} visited tiles from Firestore for user $uid")
            tiles
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Could not load visited tiles for user $uid. Ensure firestore.rules are published!", e)
            emptySet()
        }
    }

    /**
     * Saves newly visited tiles to Firestore under users/{uid}/visited_tiles in batches.
     */
    suspend fun saveVisitedTiles(uid: String, newTiles: Set<Pair<Int, Int>>) {
        if (newTiles.isEmpty()) return
        Log.d(TAG, "Saving ${newTiles.size} new visited tiles to Firestore for user $uid...")
        try {
            val collection = users.document(uid).collection("visited_tiles")
            newTiles.chunked(400).forEach { chunk ->
                val batch = Firebase.firestore.batch()
                for ((x, y) in chunk) {
                    val docRef = collection.document("${x}_$y")
                    batch.set(docRef, mapOf("x" to x, "y" to y))
                }
                batch.commit().await()
            }
            Log.d(TAG, "Successfully saved ${newTiles.size} visited tiles to Firestore for user $uid")
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Could not save visited tiles for user $uid. Ensure firestore.rules are published!", e)
        }
    }
}
