package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.abs

class SimpleElevationProvider : ElevationProvider {
    override fun getElevation(point: GeometryUtils.Point): Double {
        // Simulate elevation based on coordinates for the Alps region (approx 47N, 11E)
        // Base elevation 1000m
        // Variations using sin/cos to create peaks and valleys
        
        val lat = point.y
        val lng = point.x
        
        // Scale factors to make topography change within typical distances
        // 1 degree lat is ~111km. 
        val latScale = lat * 100.0
        val lngScale = lng * 100.0
        
        val base = 1500.0
        val variation1 = sin(latScale) * cos(lngScale) * 1000.0
        val variation2 = sin(latScale * 2.5 + 1) * sin(lngScale * 2.5 + 2) * 500.0
        val variation3 = cos(latScale * 5.0) * 200.0
        
        var elevation = base + variation1 + variation2 + variation3
        
        // Clamp to realistic values
        if (elevation < 0) elevation = 0.0
        
        return elevation
    }
}
