package com.matthaigh27.chatgptwrapper.database.repository

import androidx.lifecycle.LiveData
import com.matthaigh27.chatgptwrapper.database.dao.ImageDao
import com.matthaigh27.chatgptwrapper.database.entity.ImageEntity

class ImageRepository(private val imageDao: ImageDao) {

    val allImages: List<ImageEntity> = imageDao.getAllImages()

    suspend fun insert(image: ImageEntity) {
        imageDao.insertImage(image)
    }

    suspend fun update(image: ImageEntity) {
        imageDao.updateImage(image)
    }

    suspend fun delete(image: ImageEntity) {
        imageDao.deleteImage(image)
    }
}