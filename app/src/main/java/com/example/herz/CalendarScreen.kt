package com.example.herz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalendarScreen(
    entries: List<CheckIn>,
    today: String,
    dark: Boolean,
    onTheme: () -> Unit,
    onCheckIn: (String) -> Unit
) {
    var month by rememberSaveable { mutableStateOf(Days.month(today)) }
    var selected by rememberSaveable { mutableStateOf(today) }
    val colors = MaterialTheme.colorScheme
    val lookup = entries.associateBy { it.date }
    val entry = lookup[selected]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp),
        verticalArrangement=Arrangement.spacedBy(24.dp)) {
        BrandHeader {
            ThemeToggle(dark = dark, onClick = onTheme)
        }
        Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Heading("Your days,\nin color.")
            Text("Every feeling is part of your story.", color=colors.onSurfaceVariant)
        }
        SoftCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick={ month=Days.month(month,-1) }, modifier=Modifier.semantics { contentDescription="Previous month" }) { AppIcon("back",20.dp) }
                Text(Days.label(month,"MMMM yyyy"), Modifier.weight(1f), textAlign=androidx.compose.ui.text.style.TextAlign.Center, fontFamily=FontFamily.Serif,fontSize=22.sp)
                IconButton(onClick={ month=Days.month(month,1) }, enabled=month < Days.month(today),
                    modifier=Modifier.semantics { contentDescription="Next month" }) { AppIcon("next",20.dp) }
            }
            Row(Modifier.fillMaxWidth()) {
                listOf("M","T","W","T","F","S","S").forEach {
                    Text(it, Modifier.weight(1f), textAlign=androidx.compose.ui.text.style.TextAlign.Center, fontSize=11.sp,color=colors.onSurfaceVariant)
                }
            }
            val start = Days.mondayOffset(month)
            val length = Days.monthLength(month)
            val rows = (start + length + 6) / 7
            Column {
                repeat(rows) { row ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { col ->
                            val day = row * 7 + col - start + 1
                            if(day !in 1..length) Spacer(Modifier.weight(1f).height(48.dp))
                            else {
                                val date = Days.offset(month,day-1)
                                val dayEntry = lookup[date]
                                val isSelected = date==selected
                                Column(Modifier.weight(1f).heightIn(min=48.dp)
                                    .background(if(isSelected) colors.primaryContainer else androidx.compose.ui.graphics.Color.Transparent, CircleShape)
                                    .selectable(isSelected, enabled=date<=today, role=Role.RadioButton, onClick={ selected=date })
                                    .semantics { contentDescription="${Days.label(date)}${if(dayEntry!=null) ", ${MoodNames[dayEntry.mood-1]} mood" else ", no check-in"}" }
                                    .padding(vertical=6.dp), horizontalAlignment=Alignment.CenterHorizontally,
                                    verticalArrangement=Arrangement.spacedBy(5.dp)) {
                                    Text("$day", fontSize=13.sp, color=if(date>today) colors.outline else colors.onSurface)
                                    Box(Modifier.size(5.dp).background(dayEntry?.let { MoodColors[it.mood-1] } ?: if(date==today) colors.primary else androidx.compose.ui.graphics.Color.Transparent, CircleShape))
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color=colors.outlineVariant)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                MoodNames.forEachIndexed { index, name ->
                    Column(horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(7.dp).background(MoodColors[index],CircleShape))
                        Text(name,fontSize=10.sp,color=colors.onSurfaceVariant)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            Text(Days.label(selected,"MMMM d"),fontFamily=FontFamily.Serif,fontSize=26.sp)
            TextButton(onClick={ selected=today; month=Days.month(today) }) { Text("Today") }
        }
        if(entry != null) SoftCard {
            Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                MoodFace(entry.mood,Modifier.size(52.dp))
                Column(verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    Text("Feeling ${MoodNames[entry.mood-1].lowercase()}",fontFamily=FontFamily.Serif,fontSize=23.sp)
                    Text("Energy  ${entry.energy} / 5",color=colors.onSurfaceVariant,fontSize=13.sp)
                }
            }
            HorizontalDivider(color=colors.outlineVariant)
            Eyebrow("Your reflection")
            Text(entry.reflection.ifBlank { "A moment felt, even without words." },lineHeight=24.sp,color=colors.onSurfaceVariant)
            TextButton(onClick={ onCheckIn(selected) }) { Text(if(selected==today) "Edit today’s check-in" else "Edit this check-in") }
        } else SoftCard {
            Text("A quiet page",fontFamily=FontFamily.Serif,fontSize=24.sp)
            Text(if(selected==today) "There’s a little space here for how you feel today." else "Missed this day? You can add how you felt.",color=colors.onSurfaceVariant,lineHeight=24.sp)
            PrimaryButton(if(selected==today) "Check in with yourself" else "Add a check-in for this day", onClick={ onCheckIn(selected) })
        }
        Spacer(Modifier.height(8.dp))
    }
}
