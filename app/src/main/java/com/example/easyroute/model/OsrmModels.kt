package com.example.easyroute.model

import com.squareup.moshi.Json

// OSRM Response Structure
data class OsrmResponse(
    val routes: List<OsrmRoute>,
    val code: String
)

data class OsrmRoute(
    val geometry: OsrmGeometry,
    val distance: Double?,
    val duration: Double?
)

data class OsrmGeometry(
    val coordinates: List<List<Double>>, // [[Lon, Lat], [Lon, Lat]...]
    val type: String
)