package com.example.easyroute.model

data class FullRoute(
    val id: Int,
    val segments: List<RouteSegment>,
    val totalDistance: Double,
    val totalTime: Double
)
