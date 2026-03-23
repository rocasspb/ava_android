package com.rocasspb.avaawaand.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class GeometryUtilsTest {

    @Test
    fun testCalculateDistanceHorizontal() {
        // Distance along latitude 46.0 between 10.0 and 10.01 longitude
        val dist = GeometryUtils.calculateDistance(46.0, 10.0, 46.0, 10.01)
        // 1 deg lon at 46 lat is ~77.5km, so 0.01 deg is ~775m
        assertEquals(772.5, dist, 10.0)
    }

    @Test
    fun testCalculateDistanceVertical() {
        // Distance along longitude 10.0 between 46.0 and 46.01 latitude
        val dist = GeometryUtils.calculateDistance(46.0, 10.0, 46.01, 10.0)
        // 1 deg lat is ~111.1km, so 0.01 deg is ~1111m
        assertEquals(1111.9, dist, 5.0)
    }

    @Test
    fun testCalculateDistanceZero() {
        val dist = GeometryUtils.calculateDistance(46.0, 10.0, 46.0, 10.0)
        assertEquals(0.0, dist, 0.0001)
    }
}
