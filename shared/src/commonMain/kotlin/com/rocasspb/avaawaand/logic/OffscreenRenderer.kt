package com.rocasspb.avaawaand.logic

interface OffscreenRenderer {
    /**
     * Resets or initializes the buffer with the given dimensions.
     */
    fun init(width: Int, height: Int)

    /**
     * Draws a polygon into the buffer.
     * The color (bitmask) is applied using bitwise OR blending.
     * Points are in pixel coordinates (0..width, 0..height).
     */
    fun drawPolygon(pixelPoints: List<Pair<Float, Float>>, bitmask: Int)

    /**
     * Returns the internal pixel array (IntArray).
     */
    fun getPixels(): IntArray
}
