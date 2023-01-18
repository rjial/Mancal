package com.mancal.mancal.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.*

@Parcelize
data class Pesanan(val sepedaAwal: Sepeda, val venueTujuan: Venue, val durasi: Int) : Parcelable {
    public fun getHarga() : Int {
        return durasi * 10000;
    }
    public fun getHargaString(): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(getHarga());
    }
    public fun getAkumulasiHarga(detik: Int): String {
        return "Rp " + ((getHarga() * detik) / (durasi * 3600)).toString()
    }
}