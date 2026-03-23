package com.rocasspb.avaawaand.fakes

import com.rocasspb.avaawaand.data.GpxRepository
import com.rocasspb.avaawaand.data.GpxTrack

class FakeGpxRepository : GpxRepository {
    private val tracks = mutableMapOf<String, GpxTrack>()

    override fun saveTrack(track: GpxTrack) {
        tracks[track.id] = track
    }

    override fun getAllTracks(): List<GpxTrack> {
        return tracks.values.toList()
    }

    override fun deleteTrack(trackId: String) {
        tracks.remove(trackId)
    }
}
