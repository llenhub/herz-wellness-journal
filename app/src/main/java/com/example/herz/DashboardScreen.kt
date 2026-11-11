package com.example.herz

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun DashboardScreen(entries: List<CheckIn>, today: String, dark: Boolean, saved: Boolean,
                    onTheme: () -> Unit, onCheckIn: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val latest = entries.firstOrNull()
    val todayEntry = entries.firstOrNull { it.date == today }
    val week = weeklyEntries(entries, today)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        BrandHeader {
            ThemeToggle(dark = dark, onClick = onTheme)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(Days.label(today))
            Heading("A little more\nin tune with you.")
            Text("Make space for how you feel.", color = colors.onSurfaceVariant)
        }
        SoftCard(color = colors.primaryContainer) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Eyebrow(if(todayEntry != null) "Today’s check-in" else "A moment to pause")
                    Text(if(todayEntry == null) "How’s your Herz?" else "Feeling ${MoodNames[todayEntry.mood - 1].lowercase()}",
                        fontFamily=FontFamily.Serif, fontSize=28.sp, color=colors.onPrimaryContainer)
                    Text(if(saved) "Check-in saved. Thanks for showing up." else if(todayEntry != null) "You made time for yourself today." else "Take a breath. Let’s check in.",
                        fontSize=13.sp, lineHeight=20.sp, color=colors.onPrimaryContainer)
                    if(todayEntry != null) Text("Energy ${todayEntry.energy} / 5", fontSize=12.sp,
                        color=colors.onPrimaryContainer, fontWeight=FontWeight.SemiBold)
                }
                Spacer(Modifier.width(12.dp))
                if(todayEntry != null) MoodFace(todayEntry.mood, Modifier.size(64.dp))
                else HeartArt(Modifier.size(64.dp))
            }
            PrimaryButton(if(todayEntry == null) "Check in with yourself" else "Edit today’s check-in", onClick = onCheckIn)
        }
        SoftCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Your week in feelings", Modifier.weight(1f), fontFamily=FontFamily.Serif, fontSize=23.sp)
                Spacer(Modifier.width(8.dp))
                Text("7 DAYS", color=colors.onSurfaceVariant, fontSize=10.sp, letterSpacing=1.sp)
            }
            Text("A small picture of your inner world.", fontSize=13.sp, color=colors.onSurfaceVariant)
            WeeklyChart(entries, today)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1 Low  ·  3 Okay  ·  5 Great", fontSize=11.sp, color=colors.onSurfaceVariant)
                Text("${week.size}/7 check-ins", fontSize=11.sp, color=colors.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            SoftCard(Modifier.weight(1f), color=colors.secondaryContainer) {
                Eyebrow("Avg. energy")
                Row(verticalAlignment=Alignment.Bottom) {
                    Text(if(week.isEmpty()) "—" else String.format(Locale.getDefault(), "%.1f", week.map { it.energy }.average()), fontFamily=FontFamily.Serif, fontSize=36.sp)
                    Text(" / 5", Modifier.padding(bottom=6.dp), fontSize=13.sp)
                }
                Text("Past seven days", fontSize=11.sp)
            }
            SoftCard(Modifier.weight(1f)) {
                Eyebrow("Showing up")
                Text("${week.size}", fontFamily=FontFamily.Serif, fontSize=36.sp)
                Text(if(week.size==1) "Day this week" else "Days this week", fontSize=11.sp, color=colors.onSurfaceVariant)
            }
        }
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("A gentle perspective", fontFamily=FontFamily.Serif, fontSize=24.sp)
            Text(when {
                week.isEmpty() -> "Every feeling is a place to begin. Your first check-in will start your story."
                week.size < 3 -> "You’re making space for yourself. A few more check-ins will help you see your own patterns."
                week.map { it.mood }.average() >= 4 -> "There have been some brighter moments this week. What’s been helping you feel good?"
                week.map { it.mood }.average() < 3 -> "This week may have felt a little heavy. Be gentle with yourself, and consider reaching out to someone you trust."
                else -> "Your feelings have their own rhythm. Keep noticing what gives you energy and what asks for rest."
            }, color=colors.onSurfaceVariant, lineHeight=24.sp, fontSize=14.sp)
        }
        if(latest != null && latest.date != today) SoftCard {
            Eyebrow("Latest check-in · ${Days.label(latest.date, "MMM d")}")
            Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                MoodFace(latest.mood, Modifier.size(44.dp))
                Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text("Feeling ${MoodNames[latest.mood-1].lowercase()}", fontFamily=FontFamily.Serif, fontSize=23.sp)
                    Text("Energy ${latest.energy} / 5", color=colors.onSurfaceVariant, fontSize=13.sp)
                }
            }
        }
        if(latest != null && latest.reflection.isNotBlank()) SoftCard {
            Eyebrow("Latest reflection · ${Days.label(latest.date, "MMM d")}")
            Text("“${latest.reflection}”", fontFamily=FontFamily.Serif, fontSize=19.sp, lineHeight=28.sp)
        }
        Text("Self-reported mood & energy. A journal for reflection.", fontSize=11.sp, color=colors.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun WeeklyChart(entries: List<CheckIn>, today: String) {
    val colors = MaterialTheme.colorScheme
    val dates = Days.week(today)
    val lookup = entries.associateBy { it.date }
    val description = dates.joinToString(". ") { date ->
        "${Days.label(date, "EEEE")}: ${lookup[date]?.let { "${MoodNames[it.mood-1]}, ${it.mood} out of 5" } ?: "no check-in"}"
    }
    Column(Modifier.semantics(mergeDescendants=true) { contentDescription = description }) {
        Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            dates.forEach { date ->
                val entry = lookup[date]
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.Bottom) {
                    if(entry != null) {
                        Text("${entry.mood}", fontSize=11.sp, color=colors.onSurfaceVariant)
                        Spacer(Modifier.height(5.dp))
                        Box(Modifier.widthIn(max=28.dp).fillMaxWidth().height((entry.mood * 22).dp)
                            .background(MoodColors[entry.mood-1], androidx.compose.foundation.shape.RoundedCornerShape(10.dp)))
                    } else {
                        Box(Modifier.size(7.dp).background(colors.outlineVariant, CircleShape))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
        HorizontalDivider(color=colors.outlineVariant)
        Row(Modifier.fillMaxWidth().padding(top=10.dp), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            dates.forEach { date ->
                Text(Days.label(date, "EEE").take(1), Modifier.weight(1f), textAlign=androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize=11.sp, color=if(date==today) colors.primary else colors.onSurfaceVariant,
                    fontWeight=if(date==today) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
