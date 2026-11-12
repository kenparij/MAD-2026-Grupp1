
package com.example.townexplorer.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.view.Gravity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

//Returns true if the user has granted either fine (GPS) or coarse (network) location permission.
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
/**
 *Activates the blue location puck,
 * immediately positions it and clears fog using the device's cached location
 * and registers a LocationListener to update both the puck and the fog as the user moves.
 * Source: https://developer.android.com/reference/android/location/LocationManager
 */
@SuppressLint("MissingPermission")
fun enableUserLocation(
    context: Context,
    map: MapLibreMap,
    style: Style,
    locationManager: LocationManager
): LocationListener? {
    if (!hasLocationPermission(context)) return null

    val locationComponent = map.locationComponent
    val customOptions = LocationComponentOptions.builder(context)
        .pulseEnabled(true)
        .build()

    val activationOptions = LocationComponentActivationOptions.builder(context, style)
        .locationComponentOptions(customOptions)
        .useDefaultLocationEngine(false)
        .build()

    locationComponent.activateLocationComponent(activationOptions)
    locationComponent.isLocationComponentEnabled = true
    locationComponent.cameraMode = CameraMode.TRACKING
    locationComponent.renderMode = RenderMode.COMPASS

    val lastKnownLocation = locationManager.getProviders(true)
        .mapNotNull { providerName -> locationManager.getLastKnownLocation(providerName) }
        .maxByOrNull { it.time }

    lastKnownLocation?.let { location ->
        locationComponent.forceLocationUpdate(location)
        FogGeometryGenerator.revealFogAtLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            revealRadiusCells = 1
        )?.let { updatedFog ->
            style.getSourceAs<GeoJsonSource>("fog-source")?.setGeoJson(updatedFog)
        }
    }

    val locationListener = LocationListener { location ->
        locationComponent.forceLocationUpdate(location)

        val updatedFogGeoJson = FogGeometryGenerator.revealFogAtLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            revealRadiusCells = 1
        ) ?: return@LocationListener

        style.getSourceAs<GeoJsonSource>("fog-source")?.setGeoJson(updatedFogGeoJson)
    }

    val provider = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> return null
    }

    locationManager.requestLocationUpdates(
        provider,
        5000L,
        5f,
        locationListener
    )

    return locationListener
}


/**
 * Displays the MapLibre map inside the Home screen.
 *
 * Uses AndroidView to embed the native MapView in Compose.
 *
 * Sources:
 *   https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose
 *   https://maplibre.org/maplibre-native/android/examples/
 */

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    var isPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    var loadedMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var loadedStyleInstance by remember { mutableStateOf<Style?>(null) }
    var activeLocationListener by remember { mutableStateOf<LocationListener?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        val map = loadedMapInstance
        val style = loadedStyleInstance
        if (isPermissionGranted && map != null && style != null && activeLocationListener == null) {
            activeLocationListener = enableUserLocation(context, map, style, locationManager)
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
        MapView(context).apply { onCreate(null) }
    }

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

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            activeLocationListener?.let { listener ->
                locationManager.removeUpdates(listener)
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        map.cameraPosition = CameraPosition.Builder()
                            .target(TARTU_CENTER)
                            .zoom(14.3)
                            .build()

                        map.setLatLngBoundsForCameraTarget(FogGeometryGenerator.PLAYABLE_BOUNDS)
                        map.setMinZoomPreference(13.5)
                        map.setMaxZoomPreference(18.0)

                        map.uiSettings.apply {
                            isCompassEnabled = true
                            setCompassFadeFacingNorth(false)
                            compassGravity = Gravity.BOTTOM or Gravity.START
                            val density = resources.displayMetrics.density
                            setCompassMargins(
                                (16 * density).toInt(),
                                0,
                                0,
                                (100 * density).toInt()
                            )
                        }

                        map.setStyle(Style.Builder().fromUri(MAP_STYLE_URL)) { style ->
                            loadedMapInstance = map
                            loadedStyleInstance = style

                            // Initialize fog source with any previously explored tiles
                            val fogSource = GeoJsonSource(
                                "fog-source",
                                FogGeometryGenerator.getCurrentFog()
                            )
                            style.addSource(fogSource)

                            val fogLayer = FillLayer("fog-layer", "fog-source").withProperties(
                                PropertyFactory.fillColor("#111827"),
                                PropertyFactory.fillOpacity(0.90f),
                                PropertyFactory.fillOutlineColor("#111827")
                            )
                            style.addLayer(fogLayer)

                            if (hasLocationPermission(context) && activeLocationListener == null) {
                                activeLocationListener = enableUserLocation(
                                    context = context,
                                    map = map,
                                    style = style,
                                    locationManager = locationManager
                                )
                            }
                        }
                    }
                }
            }
        )
        val iconColor = MaterialTheme.colorScheme.onPrimaryContainer
        SmallFloatingActionButton(
            onClick = {
                val map = loadedMapInstance ?: return@SmallFloatingActionButton
                if (map.locationComponent.isLocationComponentActivated) {
                    map.locationComponent.cameraMode = CameraMode.TRACKING
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 44.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = iconColor
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val ringRadius = size.minDimension * 0.32f
                val stroke = 2.dp.toPx()

                drawCircle(color = iconColor, radius = ringRadius, style = Stroke(width = stroke))
                drawCircle(color = iconColor, radius = size.minDimension * 0.10f)

                val innerTick = ringRadius * 0.75f
                val outerTick = size.minDimension * 0.46f
                drawLine(iconColor, Offset(center.x, center.y - innerTick), Offset(center.x, center.y - outerTick), stroke, StrokeCap.Round)
                drawLine(iconColor, Offset(center.x, center.y + innerTick), Offset(center.x, center.y + outerTick), stroke, StrokeCap.Round)
                drawLine(iconColor, Offset(center.x - innerTick, center.y), Offset(center.x - outerTick, center.y), stroke, StrokeCap.Round)
                drawLine(iconColor, Offset(center.x + innerTick, center.y), Offset(center.x + outerTick, center.y), stroke, StrokeCap.Round)
            }
        }
    }
}