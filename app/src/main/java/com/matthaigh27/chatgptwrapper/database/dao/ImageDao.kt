package com.matthaigh27.chatgptwrapper.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.matthaigh27.chatgptwrapper.database.entity.ImageEntity

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(image: ImageEntity)

    @Update
    suspend fun updateImage(image: ImageEntity)

    @Delete
    suspend fun deleteImage(image: ImageEntity)

    @Query("SELECT * FROM images")
    fun getAllImages(): List<ImageEntity>
}