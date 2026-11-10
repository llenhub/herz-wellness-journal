package com.example.herz

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

val MoodNames = listOf("Low", "Meh", "Okay", "Good", "Great")
val MoodColors = listOf(Color(0xFFB9B3D6), Color(0xFFB2C7D5), Color(0xFFE7C98A), Color(0xFFB9C99F), Color(0xFFEAAC93))

@Composable
fun Heading(text: String, modifier: Modifier = Modifier, size: Int = 36) {
    Text(text, modifier, fontFamily = FontFamily.Serif, fontSize = size.sp,
        lineHeight = (size + 5).sp, letterSpacing = (-0.8).sp, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun Eyebrow(text: String) {
    Text(text.uppercase(), fontSize = 11.sp, letterSpacing = 2.sp,
        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun PrimaryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, Modifier.fillMaxWidth().heightIn(min = 58.dp), enabled = enabled,
        shape = RoundedCornerShape(20.dp)) {
        Text(label, Modifier.weight(1f, fill = false), fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.width(12.dp))
        AppIcon("arrow", 20.dp)
    }
}

@Composable
fun BrandHeader(trailing: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        HeartArt(Modifier.size(26.dp), animated = false)
        Text("herz", Modifier.padding(start = 8.dp).weight(1f), fontFamily = FontFamily.Serif,
            fontSize = 30.sp, letterSpacing = (-1).sp)
        trailing()
    }
}

@Composable
fun SoftCard(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.surface,
             content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().background(color, RoundedCornerShape(26.dp))
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(26.dp))
        .padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
}

@Composable
fun HeartArt(modifier: Modifier = Modifier, animated: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "gentle heart")
    val pulse = transition.animateFloat(1f, 1.045f,
        infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathing")
    Canvas(modifier.scale(if (animated) pulse.value else 1f)) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * .5f, h * .91f)
            cubicTo(w * .39f, h * .80f, w * .02f, h * .53f, w * .04f, h * .29f)
            cubicTo(w * .05f, h * .02f, w * .37f, -h * .02f, w * .5f, h * .23f)
            cubicTo(w * .65f, -h * .02f, w * .97f, h * .02f, w * .97f, h * .29f)
            cubicTo(w * .99f, h * .53f, w * .61f, h * .80f, w * .5f, h * .91f)
            close()
        }
        drawPath(path, Color(0xFFCD795F))
        drawOval(Color(0xFFE8A38A), Offset(w * .14f, h * .12f), Size(w * .22f, h * .12f))
        if (w > 100) {
            val ink = Color(0xFF673E31)
            drawCircle(ink, w * .017f, Offset(w * .38f, h * .43f))
            drawCircle(ink, w * .017f, Offset(w * .62f, h * .43f))
            drawArc(ink, 10f, 160f, false, Offset(w * .43f, h * .43f), Size(w * .14f, h * .12f), style = Stroke(w * .012f))
        }
    }
}

@Composable
fun MoodFace(mood: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val ink = Color(0xFF4F473B)
        drawCircle(MoodColors[mood - 1])
        drawCircle(ink, w * .033f, Offset(w * .34f, h * .40f))
        drawCircle(ink, w * .033f, Offset(w * .66f, h * .40f))
        when (mood) {
            1, 2 -> drawArc(ink, 200f, 140f, false, Offset(w * .32f, h * .60f), Size(w * .36f, h * .23f), style = Stroke(w * .035f))
            3 -> drawLine(ink, Offset(w * .35f, h * .64f), Offset(w * .65f, h * .64f), w * .035f)
            else -> drawArc(ink, 15f, 150f, false, Offset(w * .30f, h * .43f), Size(w * .40f, h * .30f), style = Stroke(w * .035f))
        }
        if (mood == 5) {
            drawCircle(Color(0xFFD78373), w * .07f, Offset(w * .23f, h * .54f))
            drawCircle(Color(0xFFD78373), w * .07f, Offset(w * .77f, h * .54f))
        }
    }
}

@Composable
fun AppIcon(name: String, size: Dp = 24.dp) {
    val ink = LocalContentColor.current
    Canvas(Modifier.size(size)) {
        val s = this.size.width
        val stroke = Stroke(s * .075f, cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round)
        val path = Path()
        when (name) {
            "home" -> {
                path.moveTo(s*.12f,s*.45f); path.lineTo(s*.5f,s*.13f); path.lineTo(s*.88f,s*.45f)
                path.moveTo(s*.22f,s*.38f); path.lineTo(s*.22f,s*.87f); path.lineTo(s*.78f,s*.87f); path.lineTo(s*.78f,s*.38f)
                path.moveTo(s*.42f,s*.87f); path.lineTo(s*.42f,s*.62f); path.lineTo(s*.59f,s*.62f); path.lineTo(s*.59f,s*.87f)
                drawPath(path, ink, style = stroke)
            }
            "calendar" -> {
                drawRoundRect(ink, Offset(s*.15f,s*.22f), Size(s*.7f,s*.65f), androidx.compose.ui.geometry.CornerRadius(s*.08f), style = stroke)
                drawLine(ink, Offset(s*.15f,s*.43f), Offset(s*.85f,s*.43f), s*.07f)
                for (x in listOf(.34f,.66f)) drawLine(ink, Offset(s*x,s*.12f), Offset(s*x,s*.32f), s*.07f)
                drawCircle(ink,s*.055f,Offset(s*.36f,s*.61f)); drawCircle(ink,s*.055f,Offset(s*.63f,s*.61f))
            }
            "sun" -> {
                drawCircle(ink,s*.20f,Offset(s*.5f,s*.5f),style=stroke)
                for (i in 0..7) {
                    val a = i * PI / 4
                    drawLine(ink,Offset(s*(.5f+.34f*cos(a).toFloat()),s*(.5f+.34f*sin(a).toFloat())),
                        Offset(s*(.5f+.44f*cos(a).toFloat()),s*(.5f+.44f*sin(a).toFloat())),s*.06f)
                }
            }
            "moon" -> {
                path.moveTo(s*.65f,s*.13f); path.cubicTo(s*.08f,s*.06f,s*.04f,s*.83f,s*.58f,s*.87f)
                path.cubicTo(s*.8f,s*.89f,s*.91f,s*.70f,s*.9f,s*.57f)
                path.cubicTo(s*.51f,s*.76f,s*.33f,s*.32f,s*.65f,s*.13f)
                drawPath(path,ink,style=stroke)
            }
            "back", "next" -> {
                val sign = if(name=="back") 1f else -1f
                path.moveTo(s*(.5f+.15f*sign),s*.22f);path.lineTo(s*(.5f-.15f*sign),s*.5f);path.lineTo(s*(.5f+.15f*sign),s*.78f)
                drawPath(path,ink,style=stroke)
            }
            else -> {
                drawLine(ink,Offset(s*.15f,s*.5f),Offset(s*.83f,s*.5f),s*.075f)
                path.moveTo(s*.55f,s*.22f);path.lineTo(s*.83f,s*.5f);path.lineTo(s*.55f,s*.78f)
                drawPath(path,ink,style=stroke)
            }
        }
    }
}
