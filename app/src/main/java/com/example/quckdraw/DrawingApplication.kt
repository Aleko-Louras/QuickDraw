package com.example.quckdraw

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DrawingApplication : Application() {

    // Coroutine scope tied to the application lifetime
    val scope = CoroutineScope(SupervisorJob())

    // Get a reference to the DB singleton
    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            DrawingDatabase::class.java,
            "drawing_database"
        ).build()
    }

    // Create the repository using lazy initialization
    val drawingRepository by lazy { DrawingRepository(scope, db.drawingDAO(), filesDir) }
}
