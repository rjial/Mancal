package com.mancal.mancal.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Invoice(
    val sepeda: Sepeda,
    val venue: Venue,
    val totalBayar: Int,
    val durasi: String): Parcelable {
}