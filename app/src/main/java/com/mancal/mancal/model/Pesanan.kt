package com.mancal.mancal.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.*

@Parcelize
data class Pesanan(val sepedaAwal: Sepeda, val venueTujuan: Venue, val durasi: Int) : Parcelable {
    private var totalBayar: Int = 0
    public fun getHarga() : Int {
        return durasi * 10000;
    }
    public fun getHargaString(): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(getHarga());
    }
    public fun getAkumulasiHarga(detik: Int): String {
        totalBayar = ((getHarga() * detik) / (durasi * 3600))
        return "Rp " + totalBayar.toString()
    }
    public fun getTotalBayar(): Int {
        return totalBayar
    }
}