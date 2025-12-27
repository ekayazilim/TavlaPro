package com.toplu.tavlauygulamasi

import android.app.Application
import androidx.room.Room
import com.toplu.tavlauygulamasi.data.db.AppDatabase

class TavlaApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "tavla_database"
        ).build()
    }
}
