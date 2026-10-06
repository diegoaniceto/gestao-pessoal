package com.gestaopessoal.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class HelloWorldScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysHelloWorld() {
        composeRule.setContent {
            HelloWorldScreen()
        }

        composeRule.onNodeWithText("Hello World").assertIsDisplayed()
    }
}
