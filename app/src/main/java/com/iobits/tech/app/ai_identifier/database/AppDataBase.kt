package com.iobits.tech.app.ai_identifier.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.database.converters.DateConverter
import com.iobits.tech.app.ai_identifier.database.dao.CollectionDao
import com.iobits.tech.app.ai_identifier.database.dataClasses.Collection


@Database(
    entities = [
        Collection::class
    ], version = 2, exportSchema = false
)

@TypeConverters(value = [DateConverter::class])
abstract class AppDataBase : RoomDatabase() {

    abstract fun getCollectionDao(): CollectionDao


    companion object {

        fun getDb(): AppDataBase {
            return dbContext
        }

        private val dbContext: AppDataBase by lazy {
            Room.databaseBuilder(MyApplication.getInstance(), AppDataBase::class.java, DB_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }
        const val DB_NAME = "AiIdentifyDb"
    }
}