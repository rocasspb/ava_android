package com.rocasspb.avaawaand.data

/**
 * Represents a single point in a GPX track.
 */
data class GpxPoint(
    val lat: Double,
    val lon: Double,
    val elevation: Double? = null
)

/**
 * Represents a summarized GPX track metadata.
 */
data class GpxMetadata(
    val name: String,
    val distance: Double,
    val elevationGain: Double,
    val elevationLoss: Double
)

/**
 * Represents a full GPX track with its geometry and metadata.
 */
data class GpxTrack(
    val id: String,
    val name: String,
    val points: List<GpxPoint>,
    val distance: Double,
    val elevationGain: Double,
    val elevationLoss: Double
)
