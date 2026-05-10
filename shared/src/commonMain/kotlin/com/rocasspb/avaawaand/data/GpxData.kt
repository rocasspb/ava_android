package com.rocasspb.avaawaand.data

import kotlinx.serialization.Serializable

/**
 * Represents a single point in a GPX track.
 */
@Serializable
data class GpxPoint(
    val lat: Double,
    val lon: Double,
    val elevation: Double? = null
)

/**
 * Represents a summarized GPX track metadata.
 */
@Serializable
data class GpxMetadata(
    val name: String,
    val distance: Double,
    val elevationGain: Double,
    val elevationLoss: Double
)

/**
 * Represents a full GPX track with its geometry and metadata.
 */
@Serializable
data class GpxTrack(
    val id: String,
    val name: String,
    val points: List<GpxPoint>,
    val distance: Double,
    val elevationGain: Double,
    val elevationLoss: Double
)
