package com.rocasspb.avaawaand.data

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * Repository for handling persistence of GPX tracks.
 */
class GpxRepository(context: Context) {
    private val gson = Gson()
    private val gpxDir = File(context.filesDir, "gpx")

    init {
        if (!gpxDir.exists()) {
            gpxDir.mkdirs()
        }
    }

    fun saveTrack(track: GpxTrack) {
        try {
            val file = File(gpxDir, "${track.id}.json")
            file.writeText(gson.toJson(track))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllTracks(): List<GpxTrack> {
        val tracks = mutableListOf<GpxTrack>()
        try {
            val files = gpxDir.listFiles { _, name -> name.endsWith(".json") }
            files?.forEach { file ->
                val track = gson.fromJson(file.readText(), GpxTrack::class.java)
                if (track != null) {
                    tracks.add(track)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tracks
    }

    fun deleteTrack(trackId: String) {
        try {
            val file = File(gpxDir, "$trackId.json")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
