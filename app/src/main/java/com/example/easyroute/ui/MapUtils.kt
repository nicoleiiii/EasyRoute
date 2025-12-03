package com.example.easyroute.ui

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import com.example.easyroute.model.JeepneyRoute
import com.example.easyroute.model.JeepneyStop
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// This simulates the "L.circleMarker" from JavaScript
fun addJeepneyStopMarker(
    map: MapView,
    stop: JeepneyStop,
    route: JeepneyRoute,
    index: Int
) {
    val marker = Marker(map)
    marker.position = stop.point

    // Create a circle drawable programmatically
    val size = 40 // Pixel size
    val drawable = ShapeDrawable(OvalShape())

    // Set color from route data
    try {
        drawable.paint.color = Color.parseColor(route.colorHex)
    } catch (e: Exception) {
        drawable.paint.color = Color.BLUE // Fallback color
    }

    drawable.paint.style = Paint.Style.FILL
    drawable.paint.strokeWidth = 2f
    drawable.intrinsicHeight = size
    drawable.intrinsicWidth = size
    marker.icon = drawable

    // Set Text Info (Popup)
    marker.title = "${route.name} (Stop ${index + 1})"
    marker.snippet = stop.name

    // Center the marker on the coordinate
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

    map.overlays.add(marker)
}