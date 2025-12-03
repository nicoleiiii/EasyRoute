package com.example.easyroute.data

import com.example.easyroute.model.OrsResponse
import com.example.easyroute.model.OrsRouteRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// Define the API endpoints
interface OrsApi {
    @POST("route") // Ensure this matches your Valhalla/ORS endpoint. For ORS it is usually v2/directions/{profile}/geojson
    suspend fun getRoute(
        @Body body: OrsRouteRequest,
        @retrofit2.http.Query("costing") costing: String = "auto" // 'auto' for car, 'pedestrian' for walk
    ): OrsResponse

    // NOTE: If using strict OpenRouteService (not Valhalla), use this signature instead:
    /*
    @POST("v2/directions/{profile}/geojson")
    suspend fun getRoute(
        @Path("profile") profile: String = "driving-car",
        @Body body: OrsRouteRequest
    ): OrsResponse
    */
}

object OrsClient {
    // Port 8002 is standard for Valhalla. If using ORS, it might be 8080. Check your Docker!
    private const val BASE_URL = "http://192.168.1.15:8002/route/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Logs network traffic to Logcat (Search for "OkHttp")
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val api: OrsApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OrsApi::class.java)
}