package com.rocasspb.avaawaand.data

import com.google.gson.annotations.SerializedName

data class AvalancheResponse(
    @SerializedName("bulletins") val bulletins: List<AvalancheData>
)

data class AvalancheData(
    @SerializedName("bulletinID") val bulletinID: String,
    @SerializedName("publicationTime") val publicationTime: String,
    @SerializedName("validTime") val validTime: ValidTime,
    @SerializedName("avalancheActivity") val avalancheActivity: AvalancheActivity?,
    @SerializedName("snowpackStructure") val snowpackStructure: SnowpackStructure?,
    @SerializedName("dangerRatings") val dangerRatings: List<DangerRating>?,
    @SerializedName("avalancheProblems") val avalancheProblems: List<AvalancheProblem>?,
    @SerializedName("tendency") val tendency: List<Tendency>?,
    @SerializedName("weatherForecast") val weatherForecast: WeatherData?,
    @SerializedName("weatherReview") val weatherReview: WeatherData?,
    @SerializedName("regions") val regions: List<Region>
)

data class ValidTime(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)

data class AvalancheActivity(
    @SerializedName("highlights") val highlights: String?,
    @SerializedName("comment") val comment: String?
)

data class SnowpackStructure(
    @SerializedName("comment") val comment: String?
)

data class DangerRating(
    @SerializedName("mainValue") val mainValue: String,
    @SerializedName("validTimePeriod") val validTimePeriod: String?,
    @SerializedName("elevation") val elevation: Elevation?
)

data class Elevation(
    @SerializedName("lowerBound") val lowerBound: String?,
    @SerializedName("upperBound") val upperBound: String?
)

data class AvalancheProblem(
    @SerializedName("problemType") val problemType: String,
    @SerializedName("elevation") val elevation: Elevation?,
    @SerializedName("validTimePeriod") val validTimePeriod: String?,
    @SerializedName("snowpackStability") val snowpackStability: String?,
    @SerializedName("frequency") val frequency: String?,
    @SerializedName("avalancheSize") val avalancheSize: Int?,
    @SerializedName("aspects") val aspects: List<String>?
)

data class Tendency(
    @SerializedName("highlights") val highlights: String?,
    @SerializedName("tendencyType") val tendencyType: String?,
    @SerializedName("validTime") val validTime: ValidTime?
)

data class WeatherData(
    @SerializedName("comment") val comment: String?
)
