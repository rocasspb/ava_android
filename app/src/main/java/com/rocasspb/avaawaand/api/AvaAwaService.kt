package com.rocasspb.avaawaand.api

import com.rocasspb.avaawaand.data.AvalancheResponse
import com.rocasspb.avaawaand.data.RegionResponse
import retrofit2.http.GET

interface AvaAwaService {
    @GET("regions")
    suspend fun getRegions(): RegionResponse

    @GET("avalanche-data")
    suspend fun getAvalancheData(): AvalancheResponse
}
