package com.example.herz

import android.os.ParcelFileDescriptor
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Dedicated emulator test: resets the test journal; restores display overrides afterward. */
class AdaptiveLayoutTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val sizes = listOf("1080x1920", "1920x1080", "1080x2092", "2208x1840", "1840x2208", "840x1600", "1920x700")

    private fun shell(command: String): String =
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
            .bufferedReader().use { it.readText() }

    private fun reachable(text: String) {
        val node = compose.onNodeWithText(text)
        if (!node.isDisplayed()) node.performScrollTo()
        node.assertIsDisplayed()
    }

    private fun resizeThroughAllSizes(scenario: ActivityScenario<MainActivity>, verify: () -> Unit) {
        for (size in sizes) {
            shell("wm size $size")
            compose.waitForIdle()
            scenario.onActivity {
                android.util.Log.i("HerzAdaptive", "$size => ${it.resources.configuration.screenWidthDp} x ${it.resources.configuration.screenHeightDp} dp")
            }
            verify()
        }
    }

    @Test fun everyScreenResizesAndRecreationPreservesProgressAndDraft() {
        val context = instrumentation.targetContext
        context.deleteDatabase("herz.db")
        context.getSharedPreferences("herz_settings", 0).edit().clear().commit()
        val originalSize = shell("wm size").lineSequence().firstOrNull { it.startsWith("Override size:") }?.substringAfter(": ")
        try {
            shell("wm size 1080x1920")
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                resizeThroughAllSizes(scenario) { reachable("Get Started") }
                scenario.recreate()
                reachable("Get Started")
                compose.onNodeWithText("Get Started").performClick()
                compose.onNodeWithContentDescription("Night theme").performScrollTo().performClick()
                resizeThroughAllSizes(scenario) {
                    compose.onNodeWithContentDescription("Night theme").assertIsOn()
                    reachable("Continue")
                }
                scenario.recreate()
                compose.onNodeWithContentDescription("Night theme").assertIsOn()
                reachable("Continue")
                compose.onNodeWithText("Continue").performClick()
                compose.onNodeWithText("Good").performScrollTo().performClick()
                compose.onNodeWithContentDescription("Energy level").performScrollTo()
                    .performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }
                compose.onNodeWithText("What’s on your mind?").performScrollTo().performTextInput("Landscape draft survives.")
                androidx.test.espresso.Espresso.closeSoftKeyboard()
                lateinit var originalModel: HerzViewModel
                scenario.onActivity { originalModel = ViewModelProvider(it)[HerzViewModel::class.java] }
                resizeThroughAllSizes(scenario) {
                    compose.onNodeWithText("Landscape draft survives.").assertExists()
                    compose.onNodeWithText("Good").assertIsSelected()
                    compose.onNodeWithContentDescription("Energy level").assertRangeInfoEquals(ProgressBarRangeInfo(4f, 1f..5f, 3))
                    reachable("Save my check-in")
                }
                scenario.recreate()
                scenario.onActivity { assertSame(originalModel, ViewModelProvider(it)[HerzViewModel::class.java]) }
                compose.onNodeWithText("Landscape draft survives.").assertExists()
                // Optional host-driven emulator fold/unfold check. No timing-based sleeps:
                // the host sends console fold/unfold, then creates the corresponding marker.
                if (InstrumentationRegistry.getArguments().getString("postureHandshake") == "true") {
                    shell("wm size reset")
                    val markers = listOf("posture-ready", "posture-folded", "posture-unfold-ready", "posture-unfolded")
                        .map { File(context.cacheDir, it) }
                    markers.forEach { it.delete() }
                    try {
                        markers[0].writeText("ready")
                        for ((index, marker) in listOf(markers[1], markers[3]).withIndex()) {
                            compose.waitUntil(90_000) { marker.exists() }
                            compose.waitForIdle()
                            scenario.onActivity {
                                android.util.Log.i("HerzAdaptive", "Posture $index: ${it.resources.configuration.screenWidthDp} x ${it.resources.configuration.screenHeightDp} dp")
                            }
                            compose.onNodeWithText("Landscape draft survives.").assertExists()
                            compose.onNodeWithText("Good").assertIsSelected()
                            compose.onNodeWithContentDescription("Energy level").assertRangeInfoEquals(ProgressBarRangeInfo(4f, 1f..5f, 3))
                            reachable("Save my check-in")
                            if (index == 0) markers[2].writeText("ready")
                        }
                    } finally { markers.forEach { it.delete() } }
                }
                reachable("Save my check-in")
                compose.onNodeWithText("Save my check-in").performClick()
                compose.waitUntil(10_000) { compose.onAllNodesWithText("Feeling good").fetchSemanticsNodes().isNotEmpty() }
                resizeThroughAllSizes(scenario) {
                    compose.onNodeWithContentDescription("Switch to light theme").assertExists()
                    reachable("Your week in feelings")
                    reachable("A gentle perspective")
                }
                compose.onNodeWithText("Calendar", useUnmergedTree = true).performClick()
                resizeThroughAllSizes(scenario) { reachable("Landscape draft survives.") }
                scenario.recreate()
                compose.onNodeWithText("Landscape draft survives.").assertExists()
                compose.onNodeWithContentDescription("Previous month").performScrollTo().performClick()
                val previousMonth = Days.month(Days.key(), -1)
                compose.onNodeWithContentDescription("${Days.label(previousMonth)}, no check-in").performScrollTo().performClick()
                scenario.recreate()
                compose.onNodeWithText(Days.label(previousMonth, "MMMM yyyy")).assertExists()
                compose.onNodeWithText(Days.label(previousMonth, "MMMM d")).assertExists()
                CheckInStore(context).use { assertEquals(1, it.all().size) }
            }
        } finally {
            shell(if (originalSize == null) "wm size reset" else "wm size $originalSize")
        }
    }
}
