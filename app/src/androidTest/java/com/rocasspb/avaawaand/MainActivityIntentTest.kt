package com.rocasspb.avaawaand

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class MainActivityIntentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testHandleGpxIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val gpxFile = File(context.cacheDir, "test.gpx")
        val gpxContent = """
            <gpx version="1.1">
                <trk><name>Test Intent</name><trkseg><trkpt lat="46.0" lon="10.0"/></trkseg></trk>
            </gpx>
        """.trimIndent()
        
        FileOutputStream(gpxFile).use { it.write(gpxContent.toByteArray()) }
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.fromFile(gpxFile), "application/gpx+xml")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // We just verify it doesn't crash. 
        // Verifying the actual import would require accessing the ViewModel, which is tricky in ActivityScenario.
        context.startActivity(intent)
    }
}
