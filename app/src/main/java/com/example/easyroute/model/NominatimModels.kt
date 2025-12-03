package com.example.easyroute.model

import com.squareup.moshi.Json

data class SearchResult(
    @Json(name = "display_name") val displayName: String,
    @Json(name = "lat") val lat: String,
    @Json(name = "lon") val lon: String
)