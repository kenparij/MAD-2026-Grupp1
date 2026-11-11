
package com.example.townexplorer.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

private const val MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

// Approximate centre of Tartu
private val TARTU_CENTER = LatLng(58.379, 26.724)


/**
 * Displays the MapLibre map inside the Home screen.
 *
 * Uses AndroidView to embed the native MapView in Compose.
 *
 * Sources:
 *   https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose
 *   https://maplibre.org/maplibre-native/android/docs/
 */

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context.applicationContext)

        MapView(context).apply {
            onCreate(null)
        }
    }

    // Connecting MapView lifecycle to Compose lifecycle.
    DisposableEffect(mapView, lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        ) {
            mapView.onStart()
        }

        if (lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.RESUMED)
        ) {
            mapView.onResume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),

        factory = {
            mapView.apply {

                getMapAsync { map ->

                    // Initial camera position
                    map.cameraPosition =
                        CameraPosition.Builder()
                            .target(TARTU_CENTER)
                            .zoom(14.3)
                            .build()

                    // Loading OpenFreeMap
                    map.setStyle(
                        Style.Builder().fromUri(MAP_STYLE_URL)
                    ) { style ->

                        // Creating fog geometry
                        val fogGeoJson =
                            FogGeometryGenerator.demoGeoJson()

                        val fogSource = GeoJsonSource(
                            "fog-source",
                            fogGeoJson
                        )

                        style.addSource(fogSource)

                        // Drawing fog above basemap labels
                        val fogLayer = FillLayer(
                            "fog-layer",
                            "fog-source"
                        ).withProperties(
                            PropertyFactory.fillColor("#111827"),
                            PropertyFactory.fillOpacity(0.90f),
                            PropertyFactory.fillOutlineColor("#111827")
                        )

                        style.addLayer(fogLayer)
                    }
                }
            }
        }
    )
}
