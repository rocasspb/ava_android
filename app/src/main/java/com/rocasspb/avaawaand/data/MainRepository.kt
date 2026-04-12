package com.rocasspb.avaawaand.data

import com.rocasspb.avaawaand.api.ApiClient
import com.rocasspb.avaawaand.api.AvaAwaService
import java.time.ZonedDateTime

interface MainRepository {
    suspend fun getRegions(): RegionResponse
    suspend fun getAvalancheData(): AvalancheResponse
    fun getPersistedRegions(): RegionResponse?
    fun getPersistedAvalancheData(): AvalancheResponse?
    fun persistRegions(regions: RegionResponse)
    fun persistAvalancheData(avalanche: AvalancheResponse)
    fun isFresh(avalancheData: AvalancheData): Boolean
    fun isDisclaimerAccepted(): Boolean
    fun setDisclaimerAccepted(accepted: Boolean)
}

class MainRepositoryImpl(
    private val apiService: AvaAwaService = ApiClient.service,
    private val persistenceManager: PersistenceManager
) : MainRepository {
    override suspend fun getRegions(): RegionResponse = apiService.getRegions()
    override suspend fun getAvalancheData(): AvalancheResponse = apiService.getAvalancheData()

    override fun getPersistedRegions(): RegionResponse? = persistenceManager.getRegions()
    override fun getPersistedAvalancheData(): AvalancheResponse? = persistenceManager.getAvalancheData()

    override fun persistRegions(regions: RegionResponse) {
        persistenceManager.saveRegions(regions)
    }

    override fun persistAvalancheData(avalanche: AvalancheResponse) {
        persistenceManager.saveAvalancheData(avalanche)
    }

    override fun isFresh(avalancheData: AvalancheData): Boolean {
        return try {
            val endTime = ZonedDateTime.parse(avalancheData.validTime.endTime)
            endTime.isAfter(ZonedDateTime.now())
        } catch (_: Exception) {
            false
        }
    }

    override fun isDisclaimerAccepted(): Boolean = persistenceManager.isDisclaimerAccepted()
    override fun setDisclaimerAccepted(accepted: Boolean) {
        persistenceManager.setDisclaimerAccepted(accepted)
    }
}
