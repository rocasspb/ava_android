package com.rocasspb.avaawaand.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvalancheResponse(
    @SerialName("bulletins") val bulletins: List<AvalancheData>
)

@Serializable
data class AvalancheData(
    @SerialName("bulletinID") val bulletinID: String,
    @SerialName("publicationTime") val publicationTime: String,
    @SerialName("validTime") val validTime: ValidTime,
    @SerialName("avalancheActivity") val avalancheActivity: AvalancheActivity? = null,
    @SerialName("snowpackStructure") val snowpackStructure: SnowpackStructure? = null,
    @SerialName("dangerRatings") val dangerRatings: List<DangerRating>? = null,
    @SerialName("avalancheProblems") val avalancheProblems: List<AvalancheProblem>? = null,
    @SerialName("tendency") val tendency: List<Tendency>? = null,
    @SerialName("weatherForecast") val weatherForecast: WeatherData? = null,
    @SerialName("weatherReview") val weatherReview: WeatherData? = null,
    @SerialName("regions") val regions: List<Region>
)

@Serializable
data class ValidTime(
    @SerialName("startTime") val startTime: String,
    @SerialName("endTime") val endTime: String
)

@Serializable
data class AvalancheActivity(
    @SerialName("highlights") val highlights: String? = null,
    @SerialName("comment") val comment: String? = null
)

@Serializable
data class SnowpackStructure(
    @SerialName("comment") val comment: String? = null
)

@Serializable
data class DangerRating(
    @SerialName("mainValue") val mainValue: String,
    @SerialName("validTimePeriod") val validTimePeriod: String? = null,
    @SerialName("elevation") val elevation: Elevation? = null
)

@Serializable
data class Elevation(
    @SerialName("lowerBound") val lowerBound: String? = null,
    @SerialName("upperBound") val upperBound: String? = null
)

@Serializable
data class AvalancheProblem(
    @SerialName("problemType") val problemType: String,
    @SerialName("elevation") val elevation: Elevation? = null,
    @SerialName("validTimePeriod") val validTimePeriod: String? = null,
    @SerialName("snowpackStability") val snowpackStability: String? = null,
    @SerialName("frequency") val frequency: String? = null,
    @SerialName("avalancheSize") val avalancheSize: Int? = null,
    @SerialName("aspects") val aspects: List<String>? = null
)

@Serializable
data class Tendency(
    @SerialName("highlights") val highlights: String? = null,
    @SerialName("tendencyType") val tendencyType: String? = null,
    @SerialName("validTime") val validTime: ValidTime? = null
)

@Serializable
data class WeatherData(
    @SerialName("comment") val comment: String? = null
)
