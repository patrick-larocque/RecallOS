package com.patricklarocque.recallos

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesHomeSurface() {
        composeTestRule
            .onNodeWithText("Home: recent memories and quick capture entry points")
            .assertExists()
    }
}
