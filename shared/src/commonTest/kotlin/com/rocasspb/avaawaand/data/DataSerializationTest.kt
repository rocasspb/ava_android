package com.rocasspb.avaawaand.data

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class DataSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testAvalancheResponseSerialization() {
        val jsonString = """
            {
                "bulletins": [
                    {
                        "bulletinID": "B123",
                        "publicationTime": "2026-05-10T12:00:00Z",
                        "validTime": {
                            "startTime": "2026-05-10T12:00:00Z",
                            "endTime": "2026-05-11T12:00:00Z"
                        },
                        "regions": [
                            {
                                "regionID": "R1",
                                "name": "Region One"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val response = json.decodeFromString<AvalancheResponse>(jsonString)
        assertEquals(1, response.bulletins.size)
        assertEquals("B123", response.bulletins[0].bulletinID)
        assertEquals("Region One", response.bulletins[0].regions[0].name)
    }

    @Test
    fun testGpxTrackSerialization() {
        val track = GpxTrack(
            id = "T1",
            name = "Test Track",
            points = listOf(GpxPoint(45.0, 10.0, 1000.0)),
            distance = 500.0,
            elevationGain = 50.0,
            elevationLoss = 10.0
        )

        val jsonString = Json.encodeToString(GpxTrack.serializer(), track)
        val decoded = Json.decodeFromString<GpxTrack>(jsonString)
        
        assertEquals(track, decoded)
    }
}
