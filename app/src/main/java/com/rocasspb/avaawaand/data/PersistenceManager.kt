package com.rocasspb.avaawaand.data

import android.content.Context
import com.rocasspb.avaawaand.utils.AvalancheConfig
import java.io.File
import androidx.core.content.edit

class PersistenceManager(context: Context) {
    private val json = AvalancheConfig.json
    private val regionsFile = File(context.filesDir, "regions.json")
    private val avalancheFile = File(context.filesDir, "avalanche.json")
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun isDisclaimerAccepted(): Boolean {
        return prefs.getBoolean("disclaimer_accepted", false)
    }

    fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit { putBoolean("disclaimer_accepted", accepted) }
    }

    fun saveRegions(response: RegionResponse) {
        try {
            regionsFile.writeText(json.encodeToString(response))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRegions(): RegionResponse? {
        return try {
            if (regionsFile.exists()) {
                json.decodeFromString<RegionResponse>(regionsFile.readText())
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun saveAvalancheData(response: AvalancheResponse) {
        try {
            avalancheFile.writeText(json.encodeToString(response))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAvalancheData(): AvalancheResponse? {
        return try {
            if (avalancheFile.exists()) {
                json.decodeFromString<AvalancheResponse>(avalancheFile.readText())
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
