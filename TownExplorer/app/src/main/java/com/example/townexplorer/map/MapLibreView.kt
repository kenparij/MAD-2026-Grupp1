
package com.example.townexplorer.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource


private const val MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

// Approximate centre of Tartu
private val TARTU_CENTER = LatLng(58.379, 26.724)

fun hasLocationPermission(context: Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineGranted || coarseGranted
}

@SuppressLint("MissingPermission")
fun enableUserLocation(context: Context, map: MapLibreMap, style: Style) {
    if (!hasLocationPermission(context)) return

    val locationComponent = map.locationComponent

    val customOptions = LocationComponentOptions.builder(context)
        .pulseEnabled(true)
        .build()

    val activationOptions = LocationComponentActivationOptions.builder(context, style)
        .locationComponentOptions(customOptions)
        .build()

    locationComponent.activateLocationComponent(activationOptions)
    locationComponent.isLocationComponentEnabled = true
    locationComponent.cameraMode = CameraMode.TRACKING
    locationComponent.renderMode = RenderMode.COMPASS
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val fogSource = style.getSourceAs<GeoJsonSource>("fog-source") ?: return

            val updatedFogGeoJson = FogGeometryGenerator.revealFogAtLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                radiusInMeters = 80.0
            )

            fogSource.setGeoJson(updatedFogGeoJson)
        }
    }

    val provider = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    provider?.let {
        locationManager.requestLocationUpdates(
            it,
            1000L,
            1f,
            locationListener
        )
    }
}


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

    var isPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    var loadedMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var loadedStyleInstance by remember { mutableStateOf<Style?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isPermissionGranted && loadedMapInstance != null && loadedStyleInstance != null) {
            enableUserLocation(context, loadedMapInstance!!, loadedStyleInstance!!)
        }
    }
    LaunchedEffect(Unit) {
        if (!isPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
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
                            FogGeometryGenerator.getInitialFog()

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

                        if (hasLocationPermission(context)) {
                            enableUserLocation(context, map, style)
                        }
                    }
                }
            }
        }
    )
}
