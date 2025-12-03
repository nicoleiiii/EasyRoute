package com.example.easyroute.model

import org.osmdroid.util.GeoPoint

data class JeepneyStop(
    val name: String,
    val point: GeoPoint
)

data class JeepneyRoute(
    val id: String,
    val name: String,
    val colorHex: String, // e.g., "#8989ff"
    val stops: List<JeepneyStop>
)