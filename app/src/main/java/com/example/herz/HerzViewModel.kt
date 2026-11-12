package com.example.herz

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Keep in-flight database operations and loaded entries across activity recreation. */
class HerzViewModel(application: Application) : AndroidViewModel(application) {
    val store = CheckInStore(application)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    var entries by mutableStateOf<List<CheckIn>>(emptyList())
        private set
    var loaded by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    var saveVersion by mutableIntStateOf(0)
        private set
    var error by mutableStateOf<String?>(null)

    init { load() }

    fun load() {
        scope.launch {
            try {
                entries = withContext(Dispatchers.IO) { store.all() }
                loaded = true
            } catch (_: Exception) {
                error = "Your check-ins couldn't be loaded. Please try again."
            }
        }
    }

    fun save(date: String, mood: Int, energy: Int, reflection: String) {
        if (date > Days.key()) {
            error = "You cannot check in for a future day."
            return
        }
        if (saving) return
        saving = true
        scope.launch {
            try {
                entries = withContext(Dispatchers.IO) {
                    store.save(CheckIn(date, mood, energy, reflection.trim()))
                    store.completeSetup()
                    store.all()
                }
                saveVersion++
            } catch (_: Exception) {
                error = "Your check-in wasn't saved. Please try again."
            } finally { saving = false }
        }
    }

    override fun onCleared() {
        scope.cancel()
        // SQLite serializes close with any outstanding database operation.
        store.close()
    }
}
