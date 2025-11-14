package com.example.easyroute.data

import retrofit2.http.GET
import retrofit2.http.Query

data class GraphHopperResponse(val paths: List<GraphHopperPath>)
data class GraphHopperPath(val points: GraphHopperPoints)
data class GraphHopperPoints(val coordinates: List<List<Double>>)

interface GraphHopperApi {
    @GET("route")
    suspend fun route(
        @Query("point") points: List<String>,
        @Query("profile") profile: String = "foot",
        @Query("key") key: String
    ): GraphHopperResponse
}
