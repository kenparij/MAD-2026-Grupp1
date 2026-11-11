package com.example.townexplorer.map

import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import kotlin.math.cos
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
    private val outerBoundary: List<Point> = listOf(
        Point.fromLngLat(26.704, 58.369),
        Point.fromLngLat(26.744, 58.369),
        Point.fromLngLat(26.744, 58.389),
        Point.fromLngLat(26.704, 58.389),
        Point.fromLngLat(26.704, 58.369)
    )
    private val exploredHoles = mutableListOf<List<Point>>()

    fun createCircleHole(
        latitude: Double,
        longitude: Double,
        radiusInMeters: Double = 80.0,
        pointsCount: Int = 32
    ): List<Point> {
        val ring = mutableListOf<Point>()
        val latRad = Math.toRadians(latitude)

        val deltaLat = radiusInMeters / 111320.0
        val deltaLng = radiusInMeters / (111320.0 * cos(latRad))

        for (i in 0 until pointsCount) {
            val angle = Math.toRadians(i * 360.0 / pointsCount)
            val lng = longitude + deltaLng * sin(angle)
            val lat = latitude + deltaLat * cos(angle)
            ring.add(Point.fromLngLat(lng, lat))
        }

        ring.add(ring.first())
        return ring
    }

    fun revealFogAtLocation(
        latitude: Double,
        longitude: Double,
        radiusInMeters: Double = 80.0
    ): FeatureCollection {
        val newHole = createCircleHole(latitude, longitude, radiusInMeters)
        exploredHoles.add(newHole)

        val allRings = mutableListOf<List<Point>>().apply {
            add(outerBoundary)
            addAll(exploredHoles)
        }

        val fogPolygon = Polygon.fromLngLats(allRings)
        val fogFeature = Feature.fromGeometry(fogPolygon)

        return FeatureCollection.fromFeature(fogFeature)
    }
    fun getInitialFog(): FeatureCollection {
        val fogPolygon = Polygon.fromLngLats(listOf(outerBoundary))
        return FeatureCollection.fromFeature(Feature.fromGeometry(fogPolygon))
    }
}
