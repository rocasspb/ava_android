package com.rocasspb.avaawaand

import com.google.gson.Gson
import com.rocasspb.avaawaand.data.AvalancheResponse
import com.rocasspb.avaawaand.data.RegionResponse
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiInspectionTest {
    @Test
    fun fetchApiStructure() {
        println("START_API_INSPECTION")
        
        val regionsJson = fetch("https://avaawa.eu/api/regions")
        if (regionsJson != null) {
            try {
                val regions = Gson().fromJson(regionsJson, RegionResponse::class.java)
                println("Parsed Regions: ${regions.features.size} features")
                assertNotNull("Regions features should not be null", regions.features)
            } catch (e: Exception) {
                println("Failed to parse Regions: ${e.message}")
                e.printStackTrace()
                throw e
            }
        } else {
             println("Failed to fetch Regions JSON")
        }

        val avalancheJson = fetch("https://avaawa.eu/api/avalanche-data")
        if (avalancheJson != null) {
            try {
                val avalanche = Gson().fromJson(avalancheJson, AvalancheResponse::class.java)
                println("Parsed Avalanche Data: ${avalanche.bulletins.size} bulletins")
                 assertNotNull("Avalanche bulletins should not be null", avalanche.bulletins)
            } catch (e: Exception) {
                println("Failed to parse Avalanche Data: ${e.message}")
                e.printStackTrace()
                 throw e
            }
        } else {
            println("Failed to fetch Avalanche JSON")
        }

        println("END_API_INSPECTION")
    }

    private fun fetch(urlString: String): String? {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            println("URL: $urlString")
            println("CODE: $responseCode")
            
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return response.toString()
            } else {
                 println("ERROR: Server returned $responseCode")
            }
        } catch (e: Exception) {
            println("ERROR fetching $urlString: ${e.message}")
            e.printStackTrace()
        }
        return null
    }
}
