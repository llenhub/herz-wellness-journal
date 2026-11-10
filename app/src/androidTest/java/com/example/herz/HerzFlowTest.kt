package com.example.herz

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class HerzFlowTest {
    @get:Rule val compose = createComposeRule()

    @Test fun onboardingSaveEditAndCalendar() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("herz.db")
        context.getSharedPreferences("herz_settings", 0).edit().clear().commit()
        compose.setContent { HerzApp() }
        compose.onNodeWithText("Get Started").performClick()
        compose.onNodeWithContentDescription("Night theme").performClick().assertIsOn()
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Save my check-in").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Good").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Energy level").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }
        compose.onNodeWithText("What’s on your mind?").performScrollTo().performTextInput("A walk helped me reset.")
        compose.onNodeWithText("Save my check-in").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Feeling good").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Switch to light theme").assertExists()
        compose.onNodeWithText("Edit today’s check-in").performScrollTo().performClick()
        compose.onNodeWithText("Great").performScrollTo().performClick()
        compose.onNodeWithText("Save my check-in").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Feeling great").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Calendar", useUnmergedTree = true).performClick()
        compose.onNodeWithText("A walk helped me reset.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Energy  4 / 5").performScrollTo().assertIsDisplayed()
        CheckInStore(context).use { store ->
            org.junit.Assert.assertEquals(1, store.all().size)
            org.junit.Assert.assertEquals(5, store.all().single().mood)
            org.junit.Assert.assertTrue(store.dark)
            org.junit.Assert.assertTrue(store.onboarded)
        }
    }
}
