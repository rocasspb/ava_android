package com.rocasspb.avaawaand.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import kotlinx.serialization.json.JsonNames

@Serializable
data class RegionResponse(
    @SerialName("type") val type: String,
    @SerialName("features") val features: List<RegionFeature>
)

@Serializable
data class RegionFeature(
    @SerialName("type") val type: String,
    @SerialName("properties") val properties: RegionProperties,
    @SerialName("geometry") val geometry: Geometry
)

@Serializable
data class RegionProperties(
    @SerialName("id") val id: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null
)

@Serializable
data class Geometry(
    @SerialName("type") val type: String,
    @SerialName("coordinates") val coordinates: List<List<List<List<Double>>>>
)

@Serializable
data class Region(
    @JsonNames("regionID", "id")
    @SerialName("regionID") val id: String,
    @SerialName("name") val name: String? = null
)
