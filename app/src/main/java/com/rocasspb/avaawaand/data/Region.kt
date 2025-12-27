package com.rocasspb.avaawaand.data

import com.google.gson.annotations.SerializedName

data class RegionResponse(
    @SerializedName("type") val type: String,
    @SerializedName("features") val features: List<RegionFeature>
)

data class RegionFeature(
    @SerializedName("type") val type: String,
    @SerializedName("properties") val properties: RegionProperties,
    @SerializedName("geometry") val geometry: Geometry
)

data class RegionProperties(
    @SerializedName("id") val id: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String?
)

data class Geometry(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<List<List<List<Double>>>>
)

data class Region(
    @SerializedName("regionID", alternate = ["id"]) val id: String,
    @SerializedName("name") val name: String
)
