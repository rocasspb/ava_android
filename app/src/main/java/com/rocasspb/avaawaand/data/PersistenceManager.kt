package com.rocasspb.avaawaand.data

import android.content.Context
import com.google.gson.Gson
import java.io.File

class PersistenceManager(context: Context) {
    private val gson = Gson()
    private val regionsFile = File(context.filesDir, "regions.json")
    private val avalancheFile = File(context.filesDir, "avalanche.json")

    fun saveRegions(response: RegionResponse) {
        try {
            regionsFile.writeText(gson.toJson(response))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRegions(): RegionResponse? {
        return try {
            if (regionsFile.exists()) {
                gson.fromJson(regionsFile.readText(), RegionResponse::class.java)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun saveAvalancheData(response: AvalancheResponse) {
        try {
            avalancheFile.writeText(gson.toJson(response))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAvalancheData(): AvalancheResponse? {
        return try {
            if (avalancheFile.exists()) {
                gson.fromJson(avalancheFile.readText(), AvalancheResponse::class.java)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
