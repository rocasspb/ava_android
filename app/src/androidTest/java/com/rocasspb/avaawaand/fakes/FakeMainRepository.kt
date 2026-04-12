package com.rocasspb.avaawaand.fakes

import com.rocasspb.avaawaand.data.*

class FakeMainRepository : MainRepository {
    private var persistedRegions: RegionResponse? = null
    private var persistedAvalanche: AvalancheResponse? = null

    override suspend fun getRegions(): RegionResponse {
        return RegionResponse("FeatureCollection", listOf(
            RegionFeature("Feature", RegionProperties("test-id", "2024-01-01", null), 
                Geometry("MultiPolygon", emptyList()))
        ))
    }

    override suspend fun getAvalancheData(): AvalancheResponse {
        return AvalancheResponse(listOf(
            AvalancheData(
                bulletinID = "bulletin-id",
                publicationTime = "2024-01-01T00:00:00Z",
                validTime = ValidTime("2024-01-01T00:00:00Z", "2024-01-02T00:00:00Z"),
                avalancheActivity = null,
                snowpackStructure = null,
                dangerRatings = null,
                avalancheProblems = null,
                tendency = null,
                weatherForecast = null,
                weatherReview = null,
                regions = listOf(Region("test-id", "Test Region"))
            )
        ))
    }

    override fun getPersistedRegions(): RegionResponse? = persistedRegions

    override fun getPersistedAvalancheData(): AvalancheResponse? = persistedAvalanche

    override fun persistRegions(regions: RegionResponse) {
        persistedRegions = regions
    }

    override fun persistAvalancheData(avalanche: AvalancheResponse) {
        persistedAvalanche = avalanche
    }

    override fun isFresh(avalancheData: AvalancheData): Boolean = true

    private var disclaimerAccepted = false
    override fun isDisclaimerAccepted(): Boolean = disclaimerAccepted
    override fun setDisclaimerAccepted(accepted: Boolean) {
        disclaimerAccepted = accepted
    }
}
