package com.rocasspb.avaawaand.data

import com.rocasspb.avaawaand.api.ApiClient
import com.rocasspb.avaawaand.api.AvaAwaService

interface MainRepository {
    suspend fun getRegions(): RegionResponse
    suspend fun getAvalancheData(): AvalancheResponse
}

class MainRepositoryImpl(private val apiService: AvaAwaService = ApiClient.service) : MainRepository {
    override suspend fun getRegions(): RegionResponse = apiService.getRegions()
    override suspend fun getAvalancheData(): AvalancheResponse = apiService.getAvalancheData()
}
