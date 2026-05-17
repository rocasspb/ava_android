package com.rocasspb.avaawaand.logic

class KorimRenderer : OffscreenRenderer {
    private var pixels: IntArray = IntArray(0)
    private var width: Int = 0
    private var height: Int = 0

    override fun init(width: Int, height: Int) {
        this.width = width
        this.height = height
        this.pixels = IntArray(width * height)
    }

    override fun drawPolygon(pixelPoints: List<Pair<Float, Float>>, bitmask: Int) {
        if (pixelPoints.size < 3) return

        val minY = pixelPoints.minOf { it.second }.toInt().coerceAtLeast(0)
        val maxY = pixelPoints.maxOf { it.second }.toInt().coerceAtMost(height - 1)

        for (y in minY..maxY) {
            val intersections = mutableListOf<Float>()
            val yFloat = y.toFloat() + 0.5f // Use center of pixel
            for (i in pixelPoints.indices) {
                val p1 = pixelPoints[i]
                val p2 = pixelPoints[(i + 1) % pixelPoints.size]

                if ((p1.second <= yFloat && p2.second > yFloat) || (p2.second <= yFloat && p1.second > yFloat)) {
                    val x = p1.first + (yFloat - p1.second) * (p2.first - p1.first) / (p2.second - p1.second)
                    intersections.add(x)
                }
            }
            intersections.sort()
            for (i in 0 until intersections.size - 1 step 2) {
                val startX = intersections[i].toInt().coerceAtLeast(0)
                val endX = intersections[i + 1].toInt().coerceAtMost(width - 1)
                for (x in startX..endX) {
                    val idx = y * width + x
                    pixels[idx] = pixels[idx] or bitmask
                }
            }
        }
    }

    override fun getPixels(): IntArray {
        return pixels
    }
}
