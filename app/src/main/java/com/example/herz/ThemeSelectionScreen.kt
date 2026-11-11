package com.example.herz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun ThemeSelectionScreen(dark: Boolean, onTheme: (Boolean) -> Unit, onContinue: () -> Unit) {
    val progress by animateFloatAsState(if (dark) 1f else 0f, tween(850, easing = FastOutSlowInEasing), label = "day to night")
    val sky by animateColorAsState(if(dark) Color(0xFF283B42) else Color(0xFFE7ECDD), tween(850), label = "sky")
    val cloud by animateColorAsState(if(dark) Color(0xFF55646B) else Color(0xFFFFFCF0), tween(850), label = "clouds")
    OnboardingLayout(content = {
        Spacer(Modifier.height(20.dp))
        Heading("What feels more\nlike you?")
        Spacer(Modifier.height(16.dp))
        Text("A little sunshine, or a little moonlight?", textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 25.sp)
        Spacer(Modifier.height(24.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(minOf(210.dp, maxWidth * .7f))
            .clip(RoundedCornerShape(120.dp)).background(sky)
            .toggleable(dark, role = Role.Switch, onValueChange = onTheme)
            .semantics { contentDescription = "Night theme" }) {
            val w = size.width; val h = size.height
            // The celestial body rotates as the whole landscape changes with it.
            rotate(progress * 180f, Offset(w*.5f,h*.48f)) {
                drawCircle(Color(0xFFF2C66E), h*.23f, Offset(w*.5f,h*.48f))
                for (i in 0..11) {
                    val a = i * PI / 6
                    val r = h*.28f; val r2 = h*.32f
                    drawLine(Color(0xFFF2C66E).copy(alpha=1-progress),
                        Offset(w*.5f+r*cos(a).toFloat(),h*.48f+r*sin(a).toFloat()),
                        Offset(w*.5f+r2*cos(a).toFloat(),h*.48f+r2*sin(a).toFloat()),5f)
                }
                drawCircle(sky, h*.21f*progress, Offset(w*.5f-h*.11f*progress,h*.48f+h*.08f*progress))
            }
            val stars = listOf(.15f to .23f,.78f to .19f,.87f to .43f,.26f to .54f,.69f to .72f)
            stars.forEach { (x,y) ->
                drawCircle(Color(0xFFFFE9AD).copy(alpha=progress),h*.011f, Offset(w*x,h*y))
            }
            fun clouds(x: Float, y: Float, scale: Float) {
                drawRoundRect(cloud, Offset(x,y),Size(h*.49f*scale,h*.13f*scale), androidx.compose.ui.geometry.CornerRadius(h*.065f))
                drawCircle(cloud,h*.10f*scale,Offset(x+h*.17f*scale,y))
                drawCircle(cloud,h*.075f*scale,Offset(x+h*.31f*scale,y+h*.01f))
            }
            clouds(w*.02f+progress*w*.05f,h*.70f,1f)
            clouds(w*.65f-progress*w*.06f,h*.42f,.85f)
        }
        }
        Spacer(Modifier.height(24.dp))
        Text(if (dark) "Moonlight" else "Sunshine", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontSize = 24.sp)
        Text("Tap the sky to switch", Modifier.padding(top=8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
      }, footer = {
        PrimaryButton("Continue", onClick = onContinue)
        Text("You can always change this later.", Modifier.padding(vertical=20.dp), fontSize=12.sp,
            color=MaterialTheme.colorScheme.onSurfaceVariant)
    })
}
