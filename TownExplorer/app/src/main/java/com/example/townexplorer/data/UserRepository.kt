package com.example.townexplorer.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private const val FLUSH_INTERVAL_MS = 10_000L // 10 seconds debounce interval

    private val users: CollectionReference
        get() = Firebase.firestore.collection("users")

    private val pendingTilesBuffer = mutableSetOf<Pair<Int, Int>>()
    private var flushJob: Job? = null

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

    private fun parseTilesFromSnapshot(snapshot: QuerySnapshot): Set<Pair<Int, Int>> {
        return snapshot.documents.mapNotNull { doc ->
            val x = doc.getLong("x")?.toInt()
            val y = doc.getLong("y")?.toInt()
            if (x != null && y != null) Pair(x, y) else null
        }.toSet()
    }

    /**
     * Fetches visited tiles for a user from Firestore (offline-first).
     * Reads from local cache first for instant startup, then updates from server.
     */
    suspend fun getVisitedTiles(uid: String): Set<Pair<Int, Int>> {
        return try {
            Log.d(TAG, "Fetching visited tiles (offline-first) for user: $uid")

            // Step 1: Read from local cache for instant UI rendering
            val cacheSnapshot = try {
                users.document(uid)
                    .collection("visited_tiles")
                    .get(Source.CACHE)
                    .await()
            } catch (_: Exception) {
                null
            }

            val cachedTiles = if (cacheSnapshot != null) parseTilesFromSnapshot(cacheSnapshot) else emptySet()
            if (cachedTiles.isNotEmpty()) {
                Log.d(TAG, "Instantly loaded ${cachedTiles.size} tiles from local Firestore cache for user $uid")
            }

            // Step 2: Fetch default (server/network) in background to pick up remote updates
            val defaultSnapshot = users.document(uid)
                .collection("visited_tiles")
                .get(Source.DEFAULT)
                .await()

            val remoteTiles = parseTilesFromSnapshot(defaultSnapshot)
            Log.d(TAG, "Synced ${remoteTiles.size} visited tiles from Firestore for user $uid")
            remoteTiles.ifEmpty { cachedTiles }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Could not load visited tiles for user $uid. Ensure firestore.rules are published!", e)
            emptySet()
        }
    }

    /**
     * Buffers newly discovered tiles and schedules a debounced batch sync after 10 seconds.
     */
    fun queueVisitedTiles(uid: String, newTiles: Set<Pair<Int, Int>>, coroutineScope: CoroutineScope) {
        if (newTiles.isEmpty()) return
        synchronized(pendingTilesBuffer) {
            pendingTilesBuffer.addAll(newTiles)
            Log.d(TAG, "Buffered ${newTiles.size} new tiles. Total pending in buffer: ${pendingTilesBuffer.size}")
        }

        if (flushJob == null || flushJob?.isCompleted == true) {
            flushJob = coroutineScope.launch {
                delay(FLUSH_INTERVAL_MS)
                flushVisitedTilesNow(uid)
            }
        }
    }

    /**
     * Immediately flushes all pending tiles in the buffer to Cloud Firestore.
     */
    suspend fun flushVisitedTilesNow(uid: String) {
        val tilesToSave = synchronized(pendingTilesBuffer) {
            val copy = pendingTilesBuffer.toSet()
            pendingTilesBuffer.clear()
            copy
        }
        if (tilesToSave.isNotEmpty()) {
            Log.d(TAG, "Flushing ${tilesToSave.size} buffered tiles to Firestore...")
            saveVisitedTiles(uid, tilesToSave)
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
