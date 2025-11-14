package com.example.easyroute.ui

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.fillMaxSize
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun OSMMap() {
    val context = LocalContext.current
    AndroidView(factory = { ctx ->
        val mapView = org.osmdroid.views.MapView(ctx)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = org.osmdroid.util.GeoPoint(14.5995, 120.9842) // Manila
        mapController.setCenter(startPoint)
        mapView
    }, modifier = Modifier.fillMaxSize())
}


@Composable
fun EasyRouteApp() {
    OSMMap()
}
