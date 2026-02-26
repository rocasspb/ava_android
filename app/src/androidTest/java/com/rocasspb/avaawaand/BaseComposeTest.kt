package com.rocasspb.avaawaand

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule

abstract class BaseComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    fun setContentWithTheme(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            AvaAwaAndTheme {
                content()
            }
        }
    }

    protected fun onNodeWithTag(tag: String) = composeTestRule.onNodeWithTag(tag)
    protected fun onNodeWithText(text: String) = composeTestRule.onNodeWithText(text)
    protected fun onNodeWithContentDescription(description: String) = composeTestRule.onNodeWithContentDescription(description)
    protected fun onNode(matcher: androidx.compose.ui.test.SemanticsMatcher) = composeTestRule.onNode(matcher)

    protected fun waitForNodeWithTag(tag: String) {
        composeTestRule.waitUntil {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
