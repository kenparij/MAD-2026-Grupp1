package com.example.townexplorer.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng

data class PointOfInterest(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0)
) {
    fun toLatLng(): LatLng = LatLng(location.latitude, location.longitude)
}

object PoiRepository {

    private const val TAG = "PoiRepository"

    private val pointsOfInterest: CollectionReference
        get() = Firebase.firestore.collection("points_of_interest")

    //fetches all points of interest form the database
    suspend fun getAllPois(): List<PointOfInterest> {
        return try {
            pointsOfInterest.get()
                .await()
                .toObjects(PointOfInterest::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch points of interest", e)
            emptyList()
        }
    }
}