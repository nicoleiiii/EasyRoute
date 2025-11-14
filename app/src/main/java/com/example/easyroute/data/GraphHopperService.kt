package com.example.easyroute.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.converter.moshi.MoshiConverterFactory

data class GHRequest(
    val points: List<List<Double>>,
    val vehicle: String = "car",
    val points_encoded: Boolean = false
)

data class GHResponse(val paths: List<GHPath>)
data class GHPath(val points: GHPoints)
data class GHPoints(val coordinates: List<List<Double>>)

interface GraphHopperApi {
    @POST("route")
    suspend fun route(
        @Query("key") key: String,
        @Body req: GHRequest
    ): GHResponse
}

object GraphHopperService {
    private val client = OkHttpClient.Builder().build()

    val api: GraphHopperApi = Retrofit.Builder()
        .baseUrl("https://graphhopper.com/api/1/") // GraphHopper cloud base
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(GraphHopperApi::class.java)
}
