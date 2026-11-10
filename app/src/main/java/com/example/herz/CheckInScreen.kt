package com.example.herz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CheckInScreen(existing: CheckIn?, saving: Boolean, onBack: () -> Unit, onSave: (Int, Int, String) -> Unit) {
    var mood by rememberSaveable { mutableIntStateOf(existing?.mood ?: 0) }
    var energy by rememberSaveable { mutableFloatStateOf(existing?.energy?.toFloat() ?: 3f) }
    var reflection by rememberSaveable { mutableStateOf(existing?.reflection ?: "") }
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !saving, modifier = Modifier.semantics { contentDescription = "Back" }) { AppIcon("back") }
            Spacer(Modifier.weight(1f)); Eyebrow("Your daily pause")
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Eyebrow(Days.label(Days.key()))
            Heading("How’s your\nHerz today?")
            Text("There’s no right or wrong way to feel.", color = colors.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("01   Your mood", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MoodNames.forEachIndexed { index, label ->
                    val selected = mood == index + 1
                    Column(Modifier.weight(1f).background(if(selected) colors.primaryContainer else colors.surface,
                        RoundedCornerShape(20.dp)).border(1.dp, if(selected) colors.primary else colors.outlineVariant, RoundedCornerShape(20.dp))
                        .selectable(selected, enabled = !saving, role = Role.RadioButton, onClick = { mood = index + 1 })
                        .padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MoodFace(index + 1, Modifier.size(38.dp))
                        Text(label, fontSize = 11.sp, fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
        SoftCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("02   Your energy", fontWeight = FontWeight.SemiBold)
                Text("${energy.roundToInt()} / 5", color = colors.primary, fontWeight = FontWeight.Bold)
            }
            Slider(energy, { energy = it }, valueRange = 1f..5f, steps = 3, enabled = !saving,
                modifier = Modifier.semantics { contentDescription = "Energy level" })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Running low", fontSize=12.sp, color=colors.onSurfaceVariant)
                Text("Fully recharged", fontSize=12.sp, color=colors.onSurfaceVariant)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("03   A little reflection", fontWeight = FontWeight.SemiBold)
                Text("Optional", fontSize=12.sp, color=colors.onSurfaceVariant)
            }
            OutlinedTextField(reflection, { if(it.length <= 2000) reflection = it }, Modifier.fillMaxWidth(),
                label = { Text("What’s on your mind?") }, placeholder = { Text("Today, I’m feeling…") },
                minLines = 3, maxLines = 6, enabled = !saving, shape = RoundedCornerShape(20.dp),
                supportingText = { Text("${reflection.length} / 2000") })
        }
        PrimaryButton(if(saving) "Saving…" else "Save my check-in", mood in 1..5 && !saving) { onSave(mood, energy.roundToInt(), reflection) }
        Text(if(mood == 0) "Choose a mood to save your check-in." else if(existing != null) "Saving will update today’s check-in." else "Just for you. Saved on this device.",
            color = colors.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))
    }
}
