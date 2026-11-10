package com.example.herz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** One scroll owner: short landscape/split windows scroll the footer along with content. */
@Composable
fun OnboardingLayout(
    content: @Composable ColumnScope.() -> Unit,
    footer: @Composable ColumnScope.() -> Unit
) {
    val scroll = rememberScrollState()
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxHeight < 480.dp || fontScale > 1.3f) {
            Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                content()
                footer()
            }
        } else {
            Column(Modifier.fillMaxSize().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll),
                    horizontalAlignment = Alignment.CenterHorizontally, content = content)
                footer()
            }
        }
    }
}
