package com.rocasspb.avaawaand

import androidx.compose.material3.Text
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleComposeTest : BaseComposeTest() {

    @Test
    fun testSimpleText() {
        setContentWithTheme {
            Text("Hello Compose")
        }
        
        composeTestRule.onNodeWithText("Hello Compose").assertExists()
    }
}
