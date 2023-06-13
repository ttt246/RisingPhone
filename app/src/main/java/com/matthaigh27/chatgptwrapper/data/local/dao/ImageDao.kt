package com.matthaigh27.chatgptwrapper.data.local.dao

import androidx.room.*
import com.matthaigh27.chatgptwrapper.data.local.entity.ImageEntity

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