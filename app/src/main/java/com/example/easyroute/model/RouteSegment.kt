package com.example.easyroute.model

data class RouteSegment(
    val id: Int,
    val startStop: JeepneyStop,
    val endStop: JeepneyStop,
    val distance: Double,
    val travelTime: Double,
    val mode: String
)
