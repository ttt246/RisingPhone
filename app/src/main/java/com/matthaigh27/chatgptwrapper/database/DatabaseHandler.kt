package com.matthaigh27.chatgptwrapper.database

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.matthaigh27.chatgptwrapper.models.ImageTableModel

//creating the database logic, extending the SQLiteOpenHelper base class
class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "RisingDatabase"
        private val TABLE_IMAGES = "ImageTable"
        private val KEY_ID = "id"
        private val KEY_PATH = "path"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //creating table with fields
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_IMAGES + "("
                + KEY_ID + " TEXT," + KEY_PATH + " TEXT" + ")")
        db?.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES)
        onCreate(db)
    }


    //method to insert data
    fun addImage(img: ImageTableModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, img.id)
        contentValues.put(KEY_PATH, img.path)

        // Inserting Row
        val success = db.insert(TABLE_IMAGES, null, contentValues)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }

    //method to read data
    @SuppressLint("Range")
    fun viewImages(): List<ImageTableModel> {
        val imgList: ArrayList<ImageTableModel> = ArrayList<ImageTableModel>()
        val selectQuery = "SELECT  * FROM $TABLE_IMAGES"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var imageId: String
        var imagePath: String
        if (cursor.moveToFirst()) {
            do {
                imageId = cursor.getString(cursor.getColumnIndex("id"))
                imagePath = cursor.getString(cursor.getColumnIndex("path"))
                val img = ImageTableModel(id = imageId, path = imagePath)
                imgList.add(img)
            } while (cursor.moveToNext())
        }
        return imgList
    }

    //method to read data
    @SuppressLint("Range")
    fun getImagePathById(id: String): String {
        val selectQuery = "SELECT  * FROM $TABLE_IMAGES WHERE id = '$id'"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ""
        }
        val imageId: String
        val imagePath: String
        if (cursor.moveToFirst()) {
            do {
                imageId = cursor.getString(cursor.getColumnIndex("id"))
                imagePath = cursor.getString(cursor.getColumnIndex("path"))
                val img = ImageTableModel(id = imageId, path = imagePath)
                return  imagePath
            } while (cursor.moveToNext())
        }
        return ""
    }

    //method to update data
    fun updateImage(img: ImageTableModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, img.id)
        contentValues.put(KEY_PATH, img.path) // EmpModelClass Name

        // Updating Row
        val success = db.update(TABLE_IMAGES, contentValues, "id=" + img.id, null)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }

    //method to delete data
    fun deleteImage(img: ImageTableModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, img.id) // EmpModelClass UserId
        // Deleting Row
        val success = db.delete(TABLE_IMAGES, "id=" + img.id, null)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }
}