package com.example.herz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 740.dp
        val artSize = minOf(if (compact) 180.dp else 270.dp, (maxWidth - 56.dp).coerceAtLeast(0.dp))
        OnboardingLayout(content = {
                BrandHeader { Eyebrow("A moment for you") }
                Spacer(Modifier.height(if(compact) 12.dp else 36.dp))
                Box(Modifier.size(artSize).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .45f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    Box(Modifier.size(artSize * .83f).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .65f), CircleShape))
                    HeartArt(Modifier.size(artSize * .61f))
                    Text("✦", Modifier.align(Alignment.TopEnd).padding(12.dp), color = MaterialTheme.colorScheme.primary, fontSize = 26.sp)
                    Text("✧", Modifier.align(Alignment.BottomStart).padding(12.dp), color = MaterialTheme.colorScheme.primary, fontSize = 30.sp)
                }
                Spacer(Modifier.height(if(compact) 20.dp else 36.dp))
                Eyebrow("Meet your daily ritual")
                Spacer(Modifier.height(12.dp))
                Text("Ready to check in\nwith your Herz?", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontSize = if(compact) 34.sp else 38.sp, lineHeight = if(compact) 39.sp else 43.sp,
                    letterSpacing = (-1).sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text("A little moment for yourself, every day.", textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 25.sp)
                Spacer(Modifier.height(20.dp))
            }, footer = {
            PrimaryButton("Get Started", onClick = onStart)
            Text("Your feelings. Your pace. Your space.", Modifier.padding(top = 12.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("A mood & energy journal · No heart-rate measurements", Modifier.padding(top = 14.dp, bottom = 20.dp), fontSize = 10.sp,
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        })
    }
}
