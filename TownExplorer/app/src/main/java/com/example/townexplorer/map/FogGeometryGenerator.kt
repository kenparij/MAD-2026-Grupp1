package com.example.townexplorer.map

import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object FogGeometryGenerator {

    /**
     * Generates fog overlay with GeoJSON.
     *
     * Coordinates use GeoJSON order:
     * [longitude, latitude]
     *
     * The returned geometry contains the playable boundary
     * and interior rings representing explored areas.
     *
     * Sources:
     *   https://maplibre.org/maplibre-native/android/examples/geojson-guide/
     *   https://datatracker.ietf.org/doc/html/rfc7946
     */
    // Grid tile size in degrees
    private const val GRID_SIZE_DEG = 0.0004

    // Outer fog boundary covering the playable city region
    private val outerBoundary: List<Point> = listOf(
        Point.fromLngLat(26.704, 58.369),
        Point.fromLngLat(26.744, 58.369),
        Point.fromLngLat(26.744, 58.389),
        Point.fromLngLat(26.704, 58.389),
        Point.fromLngLat(26.704, 58.369)
    )

    private val visitedCells = mutableSetOf<Pair<Int, Int>>()


     // Generates the initial full fog overlay.

    fun getInitialFog(): FeatureCollection {
        val fogPolygon = Polygon.fromLngLats(listOf(outerBoundary))
        return FeatureCollection.fromFeature(Feature.fromGeometry(fogPolygon))
    }

    /**
     * Reveals fog cells surrounding the user's location and returns the updated FeatureCollection.
     *
     * @param latitude Current user latitude.
     * @param longitude Current user longitude.
     * @param revealRadiusCells Grid cell radius to clear around user (1 = 3x3 grid area).
     */
    fun revealFogAtLocation(
        latitude: Double,
        longitude: Double,
        revealRadiusCells: Int = 1
    ): FeatureCollection {
        val currentCellX = floor(longitude / GRID_SIZE_DEG).toInt()
        val currentCellY = floor(latitude / GRID_SIZE_DEG).toInt()

        for (dx in -revealRadiusCells..revealRadiusCells) {
            for (dy in -revealRadiusCells..revealRadiusCells) {
                visitedCells.add(Pair(currentCellX + dx, currentCellY + dy))
            }
        }

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

        val fogPolygon = Polygon.fromLngLats(allRings)
        return FeatureCollection.fromFeature(Feature.fromGeometry(fogPolygon))
    }
}
