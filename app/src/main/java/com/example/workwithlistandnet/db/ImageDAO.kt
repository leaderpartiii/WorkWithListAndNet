package com.example.workwithlistandnet.db

import androidx.room.Dao

import androidx.room.Insert
import androidx.room.Query

@Dao
interface ImageDao {
    @Insert
    fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM images")
    fun getAllImages(): List<ImageEntity>

    @Query("DELETE FROM images")
    fun clearImages(): Int
}
