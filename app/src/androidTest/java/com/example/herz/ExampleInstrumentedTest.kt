package com.example.herz

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckInStoreTest {
    @Test fun survivesReopeningAndUpdatesWithoutDuplicates() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : android.content.ContextWrapper(target) {
            override fun getDatabasePath(name: String) = super.getDatabasePath("test_$name")
            override fun openOrCreateDatabase(name: String, mode: Int, factory: android.database.sqlite.SQLiteDatabase.CursorFactory?) =
                super.openOrCreateDatabase("test_$name", mode, factory)
            override fun openOrCreateDatabase(name: String, mode: Int, factory: android.database.sqlite.SQLiteDatabase.CursorFactory?, errorHandler: android.database.DatabaseErrorHandler?) =
                super.openOrCreateDatabase("test_$name", mode, factory, errorHandler)
        }
        target.deleteDatabase("test_herz.db")
        try {
            CheckInStore(context).use { it.save(CheckIn("2026-11-10", 3, 2, "A quiet morning")) }
            CheckInStore(context).use {
                assertEquals("A quiet morning", it.all().single().reflection)
                it.save(CheckIn("2026-11-10", 5, 4, "Better after a walk"))
                assertEquals(CheckIn("2026-11-10", 5, 4, "Better after a walk"), it.all().single())
                it.save(CheckIn("2026-11-09", 2, 1, ""))
                assertEquals(listOf("2026-11-10", "2026-11-09"), it.all().map { row -> row.date })
            }
        } finally { target.deleteDatabase("test_herz.db") }
    }
}
