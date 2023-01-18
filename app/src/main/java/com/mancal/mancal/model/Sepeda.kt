package com.mancal.mancal.model

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sepeda(val id: String, val model: String, val venue: Venue, val kunci: Boolean): Parcelable {
    val db = Firebase.firestore
    companion object {
        fun getSepedaFromID(id: String): Sepeda {
            val data = Firebase.firestore.collection("sepeda").document(id).get()

            try {
                var venue = data.result.get("venue") as DocumentReference
                var dataVenue = venue.get().result
                var objekVenue = Venue(dataVenue.get("title") as String, dataVenue.get("address") as String, (dataVenue.get("bicycleCount") as Long).toInt(), dataVenue.get("latitude") as Double, dataVenue.get("longitude") as Double)
                return Sepeda(data.result.id, data.result.get("model") as String, objekVenue, data.result.get("kunci") as Boolean)
            } catch (exc: IllegalStateException) {
                throw exc
            }

        }
    }
    public fun setKunci(): Boolean {
        var status = false
        var sepeda = db.collection("sepeda").document(this.id).update("kunci", !this.kunci)
        sepeda.addOnSuccessListener {
            status = true
        }
        sepeda.addOnFailureListener {
            status = false
            Log.e("UPDATEKUNCI", it.localizedMessage)
        }
        return status
    }

}