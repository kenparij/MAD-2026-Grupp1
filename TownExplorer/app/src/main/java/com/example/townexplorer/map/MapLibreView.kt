
package com.example.townexplorer.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
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
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.example.townexplorer.data.AuthRepository
import com.example.townexplorer.data.PointOfInterest
import com.example.townexplorer.data.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point


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
 * Activates the blue location puck,
 * immediately positions it and clears fog using the device's cached location
 * and registers a LocationListener to update both the puck and the fog as the user moves.
 *      Source: https://developer.android.com/reference/android/location/LocationManager
 */
@SuppressLint("MissingPermission")
fun enableUserLocation(
    context: Context,
    map: MapLibreMap,
    style: Style,
    locationManager: LocationManager,
    coroutineScope: CoroutineScope
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

    val initialLat = lastKnownLocation?.latitude ?: TARTU_CENTER.latitude
    val initialLng = lastKnownLocation?.longitude ?: TARTU_CENTER.longitude

    lastKnownLocation?.let { locationComponent.forceLocationUpdate(it) }

    val initialReveal = FogGeometryGenerator.revealFogAtLocation(
        latitude = initialLat,
        longitude = initialLng,
        revealRadiusCells = 1
    ) ?: if (lastKnownLocation != null) {
        // Fallback to TARTU_CENTER if lastKnownLocation is outside bounds (e.g. default emulator location)
        FogGeometryGenerator.revealFogAtLocation(
            latitude = TARTU_CENTER.latitude,
            longitude = TARTU_CENTER.longitude,
            revealRadiusCells = 1
        )
    } else null

    initialReveal?.let { revealResult ->
        style.getSourceAs<GeoJsonSource>("fog-source")?.setGeoJson(revealResult.fogGeoJson)
        val uid = AuthRepository.currentUser?.uid
        Log.d("MapLibreView", "Initial fog revealed ${revealResult.newCells.size} cells. User UID: $uid")
        if (uid != null) {
            UserRepository.queueVisitedTiles(uid, revealResult.newCells, coroutineScope)
        } else {
            Log.w("MapLibreView", "User is not logged in! Visited tiles will not be saved to Firestore until logged in.")
        }
    }

    val locationListener = LocationListener { location ->
        locationComponent.forceLocationUpdate(location)

        val revealResult = FogGeometryGenerator.revealFogAtLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            revealRadiusCells = 1
        ) ?: return@LocationListener

        style.getSourceAs<GeoJsonSource>("fog-source")?.setGeoJson(revealResult.fogGeoJson)

        val uid = AuthRepository.currentUser?.uid
        if (uid != null) {
            UserRepository.queueVisitedTiles(uid, revealResult.newCells, coroutineScope)
        }
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

private fun poisToFeatureCollection(pois: List<PointOfInterest>): FeatureCollection {
    val features = pois.map { poi ->
        Feature.fromGeometry(
            Point.fromLngLat(poi.location.longitude, poi.location.latitude)
        ).apply {
            addStringProperty("poi_id", poi.id)
        }
    }
    return FeatureCollection.fromFeatures(features)
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
    pois: List<PointOfInterest> = emptyList(),
    onPoiClick: (PointOfInterest) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentPois by rememberUpdatedState(pois)
    val currentOnPoiClick by rememberUpdatedState(onPoiClick)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = AuthRepository.currentUser

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
            activeLocationListener = enableUserLocation(context, map, style, locationManager, coroutineScope)
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

    LaunchedEffect(currentUser?.uid, loadedStyleInstance) {
        val uid = currentUser?.uid
        val style = loadedStyleInstance
        if (uid != null && style != null) {
            Log.d("MapLibreView", "Loading visited tiles from Firestore for user: $uid")
            val savedTiles = UserRepository.getVisitedTiles(uid)
            val updatedFog = FogGeometryGenerator.setVisitedCells(savedTiles)
            style.getSourceAs<GeoJsonSource>("fog-source")?.setGeoJson(updatedFog)
        } else {
            Log.d("MapLibreView", "Skipped loading tiles: uid=$uid, styleLoaded=${style != null}")
        }
    }

    LaunchedEffect(pois, loadedStyleInstance) {
        loadedStyleInstance
            ?.getSourceAs<GeoJsonSource>("poi-source")
            ?.setGeoJson(poisToFeatureCollection(pois))
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
            val uid = AuthRepository.currentUser?.uid
            if (uid != null) {
                coroutineScope.launch {
                    UserRepository.flushVisitedTilesNow(uid)
                }
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
                            setCompassFadeFacingNorth(true)
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

                            val poiSource = GeoJsonSource("poi-source", poisToFeatureCollection(currentPois))
                            style.addSource(poiSource)

                            val poiLayer = CircleLayer("poi-layer", "poi-source").withProperties(
                                PropertyFactory.circleRadius(9f),
                                PropertyFactory.circleColor("#F59E0B"),
                                PropertyFactory.circleStrokeWidth(2.5f),
                                PropertyFactory.circleStrokeColor("#FFFFFF")
                            )
                            style.addLayer(poiLayer)

                            if (hasLocationPermission(context) && activeLocationListener == null) {
                                activeLocationListener = enableUserLocation(
                                    context = context,
                                    map = map,
                                    style = style,
                                    locationManager = locationManager,
                                    coroutineScope = coroutineScope
                                )
                            }
                            
                            map.addOnMapClickListener { latLng ->
                                val screenPoint = map.projection.toScreenLocation(latLng)
                                val features = map.queryRenderedFeatures(screenPoint, "poi-layer")

                                val clickedId = features.firstOrNull()?.getStringProperty("poi_id")
                                val clickedPoi = currentPois.find { it.id == clickedId }

                                if (clickedPoi != null) {
                                    currentOnPoiClick(clickedPoi)
                                    true
                                } else {
                                    false
                                }
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