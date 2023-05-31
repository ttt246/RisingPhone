package com.matthaigh27.chatgptwrapper.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.matthaigh27.chatgptwrapper.database.entity.ContactEntity

@Dao
interface ContactDao {
    @Insert
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts")
    fun getAllImages(): List<ContactEntity>
}