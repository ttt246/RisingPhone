package com.matthaigh27.chatgptwrapper.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.matthaigh27.chatgptwrapper.database.dao.ContactDao
import com.matthaigh27.chatgptwrapper.database.dao.ImageDao
import com.matthaigh27.chatgptwrapper.database.entity.ContactEntity
import com.matthaigh27.chatgptwrapper.database.entity.ImageEntity

@Database(entities = [ImageEntity::class, ContactEntity::class], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {

    abstract fun imageDao(): ImageDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "risingphone_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}