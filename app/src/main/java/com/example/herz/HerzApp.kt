package com.example.herz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp
import com.example.herz.ui.theme.HerzTheme
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider

@Composable
fun HerzApp() {
    val activity = LocalContext.current as ComponentActivity
    val model = remember(activity) { ViewModelProvider(activity)[HerzViewModel::class.java] }
    val store = model.store
    var dark by rememberSaveable { mutableStateOf(store.dark) }
    var screen by rememberSaveable { mutableStateOf(if (store.onboarded) "home" else "welcome") }
    val entries = model.entries
    val loaded = model.loaded
    val saving = model.saving
    var saved by rememberSaveable { mutableStateOf(false) }
    var checkInDate by rememberSaveable { mutableStateOf(Days.key()) }
    var checkInReturn by rememberSaveable { mutableStateOf("home") }
    var consumedSave by rememberSaveable { mutableIntStateOf(0) }
    val screenState = rememberSaveableStateHolder()
    val view = LocalView.current
    SideEffect {
        (view.context as? android.app.Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    var today by remember { mutableStateOf(Days.key()) }
    val owner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) today = Days.key()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(model.saveVersion) {
        if (model.saveVersion == 0) consumedSave = 0 // A new model after process restoration.
        else if (model.saveVersion > consumedSave) {
            consumedSave = model.saveVersion
            screenState.removeState("checkin")
            today = Days.key()
            saved = true
            screen = checkInReturn
        }
    }
    HerzTheme(dark) {
        val colors = MaterialTheme.colorScheme
        Surface(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)), color = colors.background) {
            BackHandler(enabled = screen != "home" && screen != "welcome") {
                if (!saving) screen = when (screen) { "theme" -> "welcome"; "checkin" -> if (store.onboarded) checkInReturn else "theme"; else -> "home" }
            }
            Scaffold(containerColor = colors.background, bottomBar = {
                if (screen == "home" || screen == "calendar") {
                    NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
                        NavigationBarItem(selected = screen == "home", onClick = { screen = "home" },
                            icon = { AppIcon("home", 23.dp) }, label = { Text("Home") })
                        NavigationBarItem(selected = screen == "calendar", onClick = { screen = "calendar" },
                            icon = { AppIcon("calendar", 23.dp) }, label = { Text("Calendar") })
                    }
                }
            }) { padding ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                  Box(Modifier.widthIn(max = 560.dp).fillMaxSize()) {
                   screenState.SaveableStateProvider(screen) {
                    when (screen) {
                        "welcome" -> WelcomeScreen { screen = "theme" }
                        "theme" -> ThemeSelectionScreen(dark, { dark = it; store.setTheme(it) }) {
                            checkInDate = today
                            checkInReturn = "home"
                            screen = "checkin"
                        }
                        "checkin" -> if (loaded) CheckInScreen(checkInDate, entries.firstOrNull { it.date == checkInDate }, saving,
                            onBack = { screen = if (store.onboarded) checkInReturn else "theme" },
                            onSave = { mood, energy, reflection -> model.save(checkInDate, mood, energy, reflection) }) else CircularProgressIndicator(Modifier.align(Alignment.Center))
                        "calendar" -> CalendarScreen(
                            entries = entries,
                            today = today,
                            dark = dark,
                            onTheme = { dark = !dark; store.setTheme(dark) },
                            onCheckIn = { date ->
                                checkInDate = date
                                checkInReturn = "calendar"
                                screenState.removeState("checkin")
                                screen = "checkin"
                            }
                        )
                        else -> if (loaded) DashboardScreen(entries, today, dark, saved,
                            onTheme = { dark = !dark; store.setTheme(dark) },
                            onCheckIn = {
                                saved = false
                                checkInDate = today
                                checkInReturn = "home"
                                screenState.removeState("checkin")
                                screen = "checkin"
                            })
                        else CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                   }
                  }
                }
            }
            model.error?.let { message ->
                AlertDialog(onDismissRequest = { model.error = null }, title = { Text("A little hiccup") },
                    text = { Text(message) }, confirmButton = { TextButton(onClick = {
                        model.error = null
                        if (!loaded) model.load()
                    }) { Text(if (loaded) "OK" else "Try again") } })
            }
        }
    }
}
