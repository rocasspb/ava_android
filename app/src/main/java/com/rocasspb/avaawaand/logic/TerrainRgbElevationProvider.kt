package com.rocasspb.avaawaand.logic

import android.graphics.BitmapFactory
import android.util.Log
import com.rocasspb.avaawaand.BuildConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

class TerrainRgbElevationProvider : ElevationProvider {

    private val tileCache = ConcurrentHashMap<String, IntArray?>()
    private var currentTileZoom = 12
    private val accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    private val baseUrl = "https://api.mapbox.com/v4/mapbox.terrain-rgb"
    private val TILE_SIZE = 512

    suspend fun prepare(bounds: GeometryUtils.Bounds, mapZoom: Double) = withContext(Dispatchers.IO) {
        val zoom = floor(mapZoom).toInt()
        currentTileZoom = min(zoom + 2, 12)
        
        val minTile = lngLatToTile(bounds.minLng, bounds.maxLat, currentTileZoom)
        val maxTile = lngLatToTile(bounds.maxLng, bounds.minLat, currentTileZoom)
        
        val minTileX = min(minTile.first, maxTile.first)
        val maxTileX = max(minTile.first, maxTile.first)
        val minTileY = min(minTile.second, maxTile.second)
        val maxTileY = max(minTile.second, maxTile.second)
        
        val tilesToFetch = mutableListOf<Pair<Int, Int>>()
        for (x in minTileX..maxTileX) {
            for (y in minTileY..maxTileY) {
                tilesToFetch.add(x to y)
            }
        }
        
        // Fetch only missing tiles
        val newTiles = tilesToFetch.filter { !tileCache.containsKey("$currentTileZoom/${it.first}/${it.second}") }
        
        if (newTiles.isNotEmpty()) {
            coroutineScope {
                newTiles.map { (x, y) ->
                    async {
                        fetchTile(x, y)
                    }
                }.awaitAll()
            }
        }
    }

    private fun fetchTile(x: Int, y: Int) {
        val key = "$currentTileZoom/$x/$y"
        if (tileCache.containsKey(key)) return

        val urlString = "$baseUrl/$currentTileZoom/$x/$y@2x.pngraw?access_token=$accessToken"
        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap != null) {
                // Ensure dimensions match expectation or handle varying sizes
                // Mapbox @2x tiles are 512x512, standard is 256x256
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                tileCache[key] = pixels
                // Do not recycle if you plan to reuse, but here we only store pixels
                bitmap.recycle()
            } else {
                tileCache[key] = null
            }
        } catch (e: Exception) {
            Log.e("TerrainRgbProvider", "Error fetching tile $key: ${e.message}")
            tileCache[key] = null
        }
    }

    override fun getElevation(point: GeometryUtils.Point): Double? {
        val lng = point.x
        val lat = point.y
        
        val tileCoords = lngLatToTile(lng, lat, currentTileZoom)
        val tX = tileCoords.first
        val tY = tileCoords.second
        
        val subPixel = getSubPixelCoordinates(lng, lat, tX, tY, currentTileZoom)
        val px = subPixel.first
        val py = subPixel.second
        
        val x0 = floor(px).toInt()
        val y0 = floor(py).toInt()
        
        val dx = px - x0
        val dy = py - y0
        
        val h00 = getValue(tX, tY, x0, y0)
        val h10 = getValue(tX, tY, x0 + 1, y0)
        val h01 = getValue(tX, tY, x0, y0 + 1)
        val h11 = getValue(tX, tY, x0 + 1, y0 + 1)
        
        if (h00 == null || h10 == null || h01 == null || h11 == null) {
            return h00 ?: h10 ?: h01 ?: h11
        }
        
        val h0 = h00 * (1 - dx) + h10 * dx
        val h1 = h01 * (1 - dx) + h11 * dx
        return h0 * (1 - dy) + h1 * dy
    }
    
    private fun getValue(tx: Int, ty: Int, x: Int, y: Int): Double? {
        var targetTx = tx
        var targetTy = ty
        var targetX = x
        var targetY = y
        
        if (targetX >= TILE_SIZE) {
            val offset = floor(targetX.toDouble() / TILE_SIZE).toInt()
            targetTx += offset
            targetX -= offset * TILE_SIZE
        } else if (targetX < 0) {
            val offset = floor(targetX.toDouble() / TILE_SIZE).toInt()
            targetTx += offset
            targetX -= offset * TILE_SIZE
        }
        
        if (targetY >= TILE_SIZE) {
            val offset = floor(targetY.toDouble() / TILE_SIZE).toInt()
            targetTy += offset
            targetY -= offset * TILE_SIZE
        } else if (targetY < 0) {
            val offset = floor(targetY.toDouble() / TILE_SIZE).toInt()
            targetTy += offset
            targetY -= offset * TILE_SIZE
        }
        
        val key = "$currentTileZoom/$targetTx/$targetTy"
        val pixels = tileCache[key] ?: return null
        
        val index = targetY * TILE_SIZE + targetX
        if (index < 0 || index >= pixels.size) return null
        
        val color = pixels[index]
        // Color is ARGB Int
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        
        return getElevationFromRgb(r, g, b)
    }

    private fun getElevationFromRgb(r: Int, g: Int, b: Int): Double {
        return -10000 + ((r * 256 * 256 + g * 256 + b) * 0.1)
    }

    private fun lngLatToTile(lng: Double, lat: Double, zoom: Int): Pair<Int, Int> {
        val n = 2.0.pow(zoom)
        val x = floor((lng + 180.0) / 360.0 * n).toInt()
        val latRad = Math.toRadians(lat)
        val y = floor((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n).toInt()
        return Pair(x, y)
    }
    
    private fun getSubPixelCoordinates(lng: Double, lat: Double, x: Int, y: Int, zoom: Int): Pair<Double, Double> {
        val n = 2.0.pow(zoom)
        val xRaw = (lng + 180.0) / 360.0 * n
        val latRad = Math.toRadians(lat)
        val yRaw = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n
        
        val xRel = xRaw - x
        val yRel = yRaw - y
        
        return Pair(xRel * TILE_SIZE, yRel * TILE_SIZE)
    }
}
