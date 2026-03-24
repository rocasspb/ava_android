package com.rocasspb.avaawaand.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File

/**
 * Repository for handling persistence of GPX tracks.
 */
interface GpxRepository {
    fun saveTrack(track: GpxTrack)
    fun getAllTracks(): List<GpxTrack>
    fun deleteTrack(trackId: String)
}

class GpxRepositoryImpl(context: Context) : GpxRepository {
    private val gson = Gson()
    private val gpxDir = File(context.filesDir, "gpx")

    init {
        if (!gpxDir.exists()) {
            gpxDir.mkdirs()
        }
    }

    override fun saveTrack(track: GpxTrack) {
        try {
            val file = File(gpxDir, "${track.id}.json")
            file.writeText(gson.toJson(track))
        } catch (e: Exception) {
            Log.e("GpxRepository", "Error saving track", e)
        }
    }

    override fun getAllTracks(): List<GpxTrack> {
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
            Log.e("GpxRepository", "Error loading tracks", e)
        }
        return tracks
    }

    override fun deleteTrack(trackId: String) {
        try {
            val file = File(gpxDir, "$trackId.json")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("GpxRepository", "Error deleting track", e)
        }
    }
}
