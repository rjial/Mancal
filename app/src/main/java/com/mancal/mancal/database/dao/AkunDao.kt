package com.mancal.mancal.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface AkunDao {
    @Query("SELECT * FROM akun")
    fun getAkun()
}