package com.mancal.mancal.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "akun")
data class Akun(
    @ColumnInfo(name = "uid") val uid: String,
) {
    @PrimaryKey(autoGenerate = true) var id: Int? = null
}
