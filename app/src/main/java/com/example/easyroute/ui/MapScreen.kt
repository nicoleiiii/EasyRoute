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
fun MapScreen() {
    val context = LocalContext.current

    val mapView = MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        controller.setZoom(15.0)
        controller.setCenter(GeoPoint(14.5995, 120.9842)) // Manila
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}

@Composable
fun EasyRouteApp() {
    MapScreen()
}
