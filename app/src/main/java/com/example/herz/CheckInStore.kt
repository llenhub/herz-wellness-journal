package com.example.herz

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CheckIn(val date: String, val mood: Int, val energy: Int, val reflection: String)

object Days {
    fun key(date: Date = Date()): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    fun parse(key: String): Date = requireNotNull(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key))
    fun label(key: String, pattern: String = "EEEE, MMMM d"): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(parse(key))
    fun offset(key: String, amount: Int): String = Calendar.getInstance().run {
        time = parse(key); add(Calendar.DAY_OF_MONTH, amount); key(time)
    }
    fun week(today: String): List<String> = (-6..0).map { offset(today, it) }
    fun month(key: String, offset: Int = 0): String = Calendar.getInstance().run {
        time = parse(key); set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, offset); key(time)
    }
    fun monthLength(key: String): Int = Calendar.getInstance().run {
        time = parse(key); getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    fun mondayOffset(key: String): Int = Calendar.getInstance().run {
        time = parse(key); (get(Calendar.DAY_OF_WEEK) + 5) % 7
    }
}

fun weeklyEntries(entries: List<CheckIn>, today: String): List<CheckIn> {
    val dates = Days.week(today).toSet()
    return entries.filter { it.date in dates }
}

/** One self-reported check-in per local calendar day. No network or health sensors. */
class CheckInStore(context: Context) : SQLiteOpenHelper(context, "herz.db", null, 1) {
    private val preferences = context.getSharedPreferences("herz_settings", Context.MODE_PRIVATE)
    val onboarded: Boolean get() = preferences.getBoolean("onboarded", false)
    val dark: Boolean get() = preferences.getBoolean("dark", false)
    fun setTheme(dark: Boolean) { preferences.edit().putBoolean("dark", dark).apply() }
    fun completeSetup() { preferences.edit().putBoolean("onboarded", true).apply() }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE check_ins (date TEXT PRIMARY KEY, mood INTEGER NOT NULL CHECK(mood BETWEEN 1 AND 5), energy INTEGER NOT NULL CHECK(energy BETWEEN 1 AND 5), reflection TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    fun all(): List<CheckIn> = readableDatabase.query("check_ins", null, null, null, null, null, "date DESC").use { c ->
        buildList {
            while (c.moveToNext()) add(CheckIn(c.getString(0), c.getInt(1), c.getInt(2), c.getString(3)))
        }
    }
    fun save(entry: CheckIn) {
        require(entry.mood in 1..5 && entry.energy in 1..5)
        val values = ContentValues().apply {
            put("date", entry.date); put("mood", entry.mood)
            put("energy", entry.energy); put("reflection", entry.reflection)
        }
        writableDatabase.insertWithOnConflict("check_ins", null, values, SQLiteDatabase.CONFLICT_REPLACE).also {
            check(it != -1L) { "Could not save check-in" }
        }
    }
}
