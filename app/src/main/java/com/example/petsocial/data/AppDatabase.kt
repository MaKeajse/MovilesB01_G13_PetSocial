package com.example.petsocial.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PetProfileEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petProfileDao(): PetProfileDao

    companion object {
        @Volatile private var I: AppDatabase? = null

        fun get(ctx: Context): AppDatabase =
            I ?: synchronized(this) {
                Room.databaseBuilder(ctx, AppDatabase::class.java, "petsocial.db")
                    .build()
                    .also { I = it }
            }
    }
}
