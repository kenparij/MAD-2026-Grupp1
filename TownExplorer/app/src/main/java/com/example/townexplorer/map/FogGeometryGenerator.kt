package com.example.townexplorer.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Generates the fog-of-war overlay using GeoJSON.
 *
 * Coordinates follow GeoJSON order: [longitude, latitude].
 * The returned polygon consists of an outer fog boundary and
 * interior rings (holes) representing explored grid cells.
 *
 * Sources:
 * - https://maplibre.org/maplibre-native/android/examples/geojson-guide/
 * - https://maplibre.org/maplibre-native/android/examples/camera/lat-lng-bounds/
 */
object FogGeometryGenerator {

    // Grid tile size in degrees
    private const val GRID_SIZE_DEG = 0.0002

    val PLAYABLE_BOUNDS: LatLngBounds = LatLngBounds.Builder()
        .include(LatLng(58.369, 26.704)) // Southwest corner
        .include(LatLng(58.389, 26.744)) // Northeast corner
        .build()

    private const val OUTER_MIN_LNG = 26.670
    private const val OUTER_MAX_LNG = 26.780
    private const val OUTER_MIN_LAT = 58.350
    private const val OUTER_MAX_LAT = 58.410

    private val outerBoundary: List<Point> = listOf(
        Point.fromLngLat(OUTER_MIN_LNG, OUTER_MIN_LAT),
        Point.fromLngLat(OUTER_MAX_LNG, OUTER_MIN_LAT),
        Point.fromLngLat(OUTER_MAX_LNG, OUTER_MAX_LAT),
        Point.fromLngLat(OUTER_MIN_LNG, OUTER_MAX_LAT),
        Point.fromLngLat(OUTER_MIN_LNG, OUTER_MIN_LAT)
    )

    private val visitedCells = mutableSetOf<Pair<Int, Int>>()


    // Builds and returns the full fog-of-war GeoJSON polygon, cutting out holes for every grid cell saved in visitedCells
    fun getCurrentFog(): FeatureCollection {
        val holeRings = visitedCells.map { (cellX, cellY) ->
            val minLng = cellX * GRID_SIZE_DEG
            val maxLng = (cellX + 1) * GRID_SIZE_DEG
            val minLat = cellY * GRID_SIZE_DEG
            val maxLat = (cellY + 1) * GRID_SIZE_DEG

            listOf(
                Point.fromLngLat(minLng, maxLat),
                Point.fromLngLat(maxLng, maxLat),
                Point.fromLngLat(maxLng, minLat),
                Point.fromLngLat(minLng, minLat),
                Point.fromLngLat(minLng, maxLat)
            )
        }

        val allRings = mutableListOf<List<Point>>().apply {
            add(outerBoundary)
            addAll(holeRings)
        }

        return FeatureCollection.fromFeature(
            Feature.fromGeometry(Polygon.fromLngLats(allRings))
        )
    }

    /**
     * Marks the grid cells around the user's coordinates as visited and returns the updated fog GeoJSON—or null if no new cells were entered
     */
    fun revealFogAtLocation(
        latitude: Double,
        longitude: Double,
        revealRadiusCells: Int = 1
    ): FeatureCollection? {
        val currentCellX = floor(longitude / GRID_SIZE_DEG).toInt()
        val currentCellY = floor(latitude / GRID_SIZE_DEG).toInt()
        var hasNewCells = false

        for (dx in -revealRadiusCells..revealRadiusCells) {
            for (dy in -revealRadiusCells..revealRadiusCells) {
                val cellX = currentCellX + dx
                val cellY = currentCellY + dy

                if (!isCellInsideOuterBoundary(cellX, cellY)) continue

                if (visitedCells.add(Pair(cellX, cellY))) {
                    hasNewCells = true
                }
            }
        }

        if (!hasNewCells) return null
        return getCurrentFog()
    }

    //Checks that a grid cell lies strictly inside the outer fog boundary
    private fun isCellInsideOuterBoundary(cellX: Int, cellY: Int): Boolean {
        val minLng = cellX * GRID_SIZE_DEG
        val maxLng = (cellX + 1) * GRID_SIZE_DEG
        val minLat = cellY * GRID_SIZE_DEG
        val maxLat = (cellY + 1) * GRID_SIZE_DEG

        return minLng > OUTER_MIN_LNG &&
                maxLng < OUTER_MAX_LNG &&
                minLat > OUTER_MIN_LAT &&
                maxLat < OUTER_MAX_LAT
    }
}
