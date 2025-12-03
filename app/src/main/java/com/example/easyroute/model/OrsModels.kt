package com.example.easyroute.model

import com.squareup.moshi.Json

// 1. Request Body (What we send to the server)
data class OrsRouteRequest(
    val coordinates: List<List<Double>> // [[Lon, Lat], [Lon, Lat]]
)

// 2. Response Body (What the server sends back)
data class OrsResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry,
    val properties: Properties? = null
)

data class Geometry(
    val coordinates: List<List<Double>> // List of [Lon, Lat] points
)

data class Properties(
    val summary: Summary?
)

data class Summary(
    val distance: Double, // Meters
    val duration: Double  // Seconds
)