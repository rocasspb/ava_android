package com.rocasspb.avaawaand.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rocasspb.avaawaand.BaseComposeTest
import com.rocasspb.avaawaand.MainViewModel
import com.rocasspb.avaawaand.PointInfoCard
import com.rocasspb.avaawaand.data.AvalancheActivity
import com.rocasspb.avaawaand.data.AvalancheProblem
import com.rocasspb.avaawaand.data.DangerRating
import com.rocasspb.avaawaand.data.Elevation
import androidx.compose.ui.test.hasText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PointInfoCardTest : BaseComposeTest() {

    @Test
    fun testPointInfoCardDisplaysData() {
        val pointInfo = MainViewModel.PointInfo(
            elevation = 2100,
            slope = 34.5,
            aspect = "NE",
            dangerRatings = listOf(
                DangerRating(mainValue = "considerable", validTimePeriod = null, elevation = Elevation("1800", null))
            ),
            avalancheProblems = listOf(
                AvalancheProblem(
                    problemType = "wind_slab",
                    elevation = Elevation("2000", null),
                    validTimePeriod = null,
                    snowpackStability = "poor",
                    frequency = "many",
                    avalancheSize = 2,
                    aspects = listOf("N", "NE", "E")
                )
            ),
            avalancheActivity = AvalancheActivity(
                highlights = "Danger highlights",
                comment = "Danger comment"
            )
        )
        
        setContentWithTheme {
            PointInfoCard(pointInfo = pointInfo)
        }
        
        onNode(hasText("2100m")).assertExists()
        onNode(hasText("34.5°")).assertExists()
        onNode(hasText("NE")).assertExists()
        onNode(hasText("Considerable")).assertExists()
        onNode(hasText("Wind slab")).assertExists()
        onNode(hasText("Danger highlights", substring = true)).assertExists()
        onNode(hasText("Danger comment", substring = true)).assertExists()
    }
}
