package com.example.easyroute.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.easyroute.data.JeepneyData
import com.example.easyroute.data.NominatimClient
import com.example.easyroute.data.OsrmClient
import com.example.easyroute.data.SearchResult
import com.example.easyroute.model.JeepneyRoute
import com.example.easyroute.model.SegmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// Note: addJeepneyStopMarker is accessed from MapUtils.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- STATE ---
    var mapViewState by remember { mutableStateOf<MapView?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedRoute by remember { mutableStateOf<JeepneyRoute?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var estimatedTimeText by remember { mutableStateOf("") }

    // Search & UI State
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var destinationMarker by remember { mutableStateOf<Marker?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var isSearchBarActive by remember { mutableStateOf(false) }

    // Permission State
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val startPoint = GeoPoint(15.1400, 120.5900)

    // --- PERMISSION LAUNCHER ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            hasLocationPermission = true
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName

        if (!hasLocationPermission) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // --- HELPER: CLEAR MAP ---
    fun clearMap(map: MapView) {
        val overlaysToRemove = map.overlays.filter { overlay ->
            if (overlay is Polyline) return@filter true
            if (overlay is Marker) return@filter true
            return@filter false
        }
        map.overlays.removeAll(overlaysToRemove)
        map.invalidate()
    }

    // --- LOGIC 1: MANUAL ROUTE SELECTION ---
    fun loadRoadBasedRoute(map: MapView, route: JeepneyRoute, currentUserLoc: GeoPoint?) {
        scope.launch {
            isLoading = true
            clearMap(map)
            estimatedTimeText = ""

            route.stops.forEachIndexed { index, stop ->
                addJeepneyStopMarker(map, stop, route, index)
            }

            try {
                val jeepCoords = route.stops.joinToString(";") { "${it.point.longitude},${it.point.latitude}" }
                val jeepResponse = OsrmClient.api.getRoute(coordinates = jeepCoords)
                if (jeepResponse.routes.isNotEmpty()) {
                    val shape = jeepResponse.routes[0].geometry.coordinates.map { GeoPoint(it[1], it[0]) }
                    val jeepLine = Polyline()
                    jeepLine.setPoints(shape)
                    try { jeepLine.color = AndroidColor.parseColor(route.colorHex) }
                    catch (e: Exception) { jeepLine.color = AndroidColor.BLUE }
                    jeepLine.width = 15f
                    jeepLine.title = route.name
                    map.overlays.add(0, jeepLine)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val fallback = Polyline()
                fallback.setPoints(route.stops.map { it.point })
                fallback.color = AndroidColor.GRAY
                map.overlays.add(0, fallback)
            }

            if (currentUserLoc != null) {
                try {
                    val result = withContext(Dispatchers.Default) {
                        JeepneyData.findNearestStop(currentUserLoc, route.stops)
                    }
                    if (result != null) {
                        val (nearestStop, _) = result
                        val walkCoords = "${currentUserLoc.longitude},${currentUserLoc.latitude};${nearestStop.point.longitude},${nearestStop.point.latitude}"
                        val walkResponse = OsrmClient.api.getRoute(coordinates = walkCoords)
                        if (walkResponse.routes.isNotEmpty()) {
                            val walkShape = walkResponse.routes[0].geometry.coordinates.map { GeoPoint(it[1], it[0]) }
                            val walkLine = Polyline()
                            walkLine.setPoints(walkShape)
                            walkLine.color = AndroidColor.parseColor("#FF5733")
                            walkLine.width = 12f
                            walkLine.outlinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
                            walkLine.outlinePaint.strokeCap = Paint.Cap.ROUND
                            map.overlays.add(walkLine)
                        }
                    }
                } catch (e: Exception) { }
            }
            map.invalidate()
            isLoading = false
        }
    }

    // --- LOGIC 2: DESTINATION ROUTING ---
    fun multiRouteToDestination(map: MapView, destinationLatLng: GeoPoint, currentUserLoc: GeoPoint?) {
        if (currentUserLoc == null) {
            Toast.makeText(context, "Waiting for user location...", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isLoading = true
            clearMap(map)
            estimatedTimeText = "Calculating..."

            val destMarker = Marker(map)
            destMarker.position = destinationLatLng
            destMarker.title = "Destination"
            destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(destMarker)
            destinationMarker = destMarker

            val tripPlan = withContext(Dispatchers.Default) {
                JeepneyData.calculateMultiRouteTrip(currentUserLoc, destinationLatLng)
            }

            if (tripPlan == null) {
                Toast.makeText(context, "No route found to destination", Toast.LENGTH_SHORT).show()
                isLoading = false
                estimatedTimeText = "No Route Found"
                return@launch
            }

            val mins = (tripPlan.totalTimeSeconds / 60).toInt()
            estimatedTimeText = "Est. Time: $mins mins"
            selectedRoute = tripPlan.segments.find { it.type == SegmentType.RIDE }?.route

            val allPoints = ArrayList<GeoPoint>()
            allPoints.add(currentUserLoc)
            allPoints.add(destinationLatLng)

            tripPlan.segments.forEach { segment ->
                if (segment.type == SegmentType.RIDE && segment.route != null) {
                    segment.points.forEachIndexed { i, pt ->
                        val stopObj = segment.route.stops.find {
                            it.point.latitude == pt.latitude && it.point.longitude == pt.longitude
                        }
                        if (stopObj != null) addJeepneyStopMarker(map, stopObj, segment.route, i)
                    }
                }

                try {
                    val coords = if (segment.type == SegmentType.RIDE) {
                        segment.points.joinToString(";") { "${it.longitude},${it.latitude}" }
                    } else {
                        "${segment.points.first().longitude},${segment.points.first().latitude};${segment.points.last().longitude},${segment.points.last().latitude}"
                    }
                    val response = OsrmClient.api.getRoute(coordinates = coords)
                    if (response.routes.isNotEmpty()) {
                        val shape = response.routes[0].geometry.coordinates.map { GeoPoint(it[1], it[0]) }
                        allPoints.addAll(shape)
                        val line = Polyline()
                        line.setPoints(shape)
                        if (segment.type == SegmentType.WALK) {
                            line.color = AndroidColor.parseColor("#FF5733")
                            line.width = 10f
                            line.outlinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
                            line.outlinePaint.strokeCap = Paint.Cap.ROUND
                        } else {
                            try { line.color = AndroidColor.parseColor(segment.route?.colorHex ?: "#0000FF") }
                            catch (e: Exception) { line.color = AndroidColor.BLUE }
                            line.width = 15f
                        }
                        if (segment.type == SegmentType.RIDE) map.overlays.add(0, line)
                        else map.overlays.add(line)
                    }
                } catch (e: Exception) {
                    val fallback = Polyline()
                    fallback.setPoints(segment.points)
                    fallback.color = AndroidColor.GRAY
                    map.overlays.add(fallback)
                }
            }
            val bbox = BoundingBox.fromGeoPoints(allPoints)
            map.zoomToBoundingBox(bbox, true, 100)
            isLoading = false
            map.invalidate()
        }
    }

    // --- UI STRUCTURE ---
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. MAP VIEW
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(startPoint)

                    if (hasLocationPermission) {
                        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        overlay.enableMyLocation()
                        overlay.enableFollowLocation()
                        overlay.runOnFirstFix { userLocation = overlay.myLocation }
                        overlays.add(overlay)
                    }
                    mapViewState = this
                }
            },
            update = { map ->
                if (hasLocationPermission && !map.overlays.any { it is MyLocationNewOverlay }) {
                    val overlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                    overlay.enableMyLocation()
                    overlay.enableFollowLocation()
                    overlay.runOnFirstFix { userLocation = overlay.myLocation }
                    map.overlays.add(overlay)
                }
            }
        )

        // 2. INFO CARD (Updated placement with safe padding)
        if (estimatedTimeText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Pushes card up above the navigation bar + spacing
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = estimatedTimeText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }

        // 3. SEARCH & DROPDOWN (Updated placement with safe padding)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                // Pushes content down below the status bar
                .statusBarsPadding()
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // A. Search Bar
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    if (searchQuery.isNotEmpty()) {
                        isSearching = true
                        scope.launch {
                            try { searchResults = NominatimClient.api.search(searchQuery) }
                            catch (e: Exception) { e.printStackTrace() }
                            finally { isSearching = false }
                        }
                    }
                },
                active = isSearchBarActive,
                onActiveChange = { isSearchBarActive = it },
                placeholder = { Text("Search destination...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchResults = emptyList()
                            isSearchBarActive = false
                            estimatedTimeText = ""
                            mapViewState?.let { clearMap(it) }
                        }) { Icon(Icons.Default.Close, contentDescription = "Clear") }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                    if (isSearching) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                    items(searchResults) { result ->
                        ListItem(
                            headlineContent = { Text(result.displayName) },
                            modifier = Modifier.clickable {
                                val lat = result.lat.toDouble()
                                val lon = result.lon.toDouble()
                                val destPoint = GeoPoint(lat, lon)

                                mapViewState?.let { map ->
                                    map.controller.animateTo(destPoint)
                                    if (destinationMarker != null) map.overlays.remove(destinationMarker)
                                    destinationMarker = Marker(map).apply {
                                        position = destPoint
                                        title = "Destination"
                                        snippet = result.displayName
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    }
                                    map.overlays.add(destinationMarker)
                                    val overlay = map.overlays.firstOrNull { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                                    if (overlay?.myLocation != null) userLocation = overlay.myLocation
                                    multiRouteToDestination(map, destPoint, userLocation)
                                }
                                isSearchBarActive = false
                                searchQuery = ""
                                searchResults = emptyList()
                            }
                        )
                        Divider()
                    }
                }
            }

            // B. JEEPNEY ROUTES BUTTON
            if (!isSearchBarActive) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(text = selectedRoute?.name ?: "Select Jeepney Route")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        JeepneyData.routes.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route.name) },
                                onClick = {
                                    expanded = false
                                    selectedRoute = route
                                    val overlay = mapViewState?.overlays?.firstOrNull { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                                    if (overlay?.myLocation != null) userLocation = overlay.myLocation
                                    mapViewState?.let { loadRoadBasedRoute(it, route, userLocation) }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 4. REFRESH BUTTON (Updated placement with safe padding)
        if (selectedRoute != null) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // Pushes button up above the navigation bar
                    .navigationBarsPadding()
                    .padding(16.dp),
                onClick = {
                    val overlay = mapViewState?.overlays?.firstOrNull { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                    if (overlay?.myLocation != null) userLocation = overlay.myLocation

                    if (estimatedTimeText.isNotEmpty() && destinationMarker != null) {
                        mapViewState?.let { multiRouteToDestination(it, destinationMarker!!.position, userLocation) }
                    } else {
                        mapViewState?.let { loadRoadBasedRoute(it, selectedRoute!!, userLocation) }
                    }
                }
            ) { Text("↻") }
        }

        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}