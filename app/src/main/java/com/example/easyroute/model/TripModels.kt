package com.example.easyroute.model

import org.osmdroid.util.GeoPoint

enum class SegmentType {
    WALK, RIDE
}

data class TripSegment(
    val type: SegmentType,
    val route: JeepneyRoute? = null, // Null if walking
    val points: List<GeoPoint>,      // The stops or points involved
    val distanceMeters: Double,
    val description: String
)

data class TripPlan(
    val segments: List<TripSegment>,
    val totalTimeSeconds: Double,
    val totalDistanceMeters: Double
)