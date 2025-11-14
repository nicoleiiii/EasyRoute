package com.example.easyroute.ui

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun OSMMapScreen() {
    val ctx = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(14.5995, 120.9842))
                }.also { mapView ->
                    mapViewRef = mapView
                }
            },
            update = { view ->
                // No-op for now
            }
        )

        // Very simple control row to demo adding a marker & route
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                mapViewRef?.let { addMarker(it, 14.5995, 120.9842, "Start: Manila") }
            }) {
                Text("Add Marker")
            }

            Button(onClick = {
                // Demo route: draw a simple polyline between two points
                val routePoints = listOf(
                    GeoPoint(14.5995, 120.9842),
                    GeoPoint(14.6050, 120.9820)
                )
                mapViewRef?.let { drawRoute(it, routePoints) }
            }) {
                Text("Draw Demo Route")
            }
        }
    }
}

fun addMarker(mapView: MapView, lat: Double, lon: Double, label: String) {
    // Must run on UI thread
    Handler(Looper.getMainLooper()).post {
        val marker = Marker(mapView).apply {
            position = GeoPoint(lat, lon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = label
        }
        mapView.overlays.add(marker)
        mapView.invalidate()
    }
}

fun drawRoute(mapView: MapView, points: List<GeoPoint>) {
    Handler(Looper.getMainLooper()).post {
        val polyline = Polyline().apply {
            setPoints(points)
            width = 8f
            color = Color.RED
        }
        mapView.overlays.add(polyline)
        mapView.invalidate()
    }
}
