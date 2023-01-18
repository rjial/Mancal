package com.mancal.mancal

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mancal.mancal.model.Pesanan
import com.mancal.mancal.model.Sepeda
import com.mancal.mancal.model.Venue
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

class Activity_ReviewPesanan : AppCompatActivity() {
    private lateinit var venueAwal: Venue
    private lateinit var sepedaAwal: Sepeda
    private lateinit var venueTujuan: Venue
    private var durasiPerjalanan: Int = 1
    private val spDurasiPesanan: Spinner by lazy {
        findViewById<Spinner>(R.id.spDurasiPesanan)
    }
    private val alamatvenueawal: TextView by lazy {
        findViewById<TextView>(R.id.alamatvenueawal)
    }
    private val alamattujuanvenue: TextView by lazy {
        findViewById<TextView>(R.id.alamattujuanvenue)
    }
    private val textjenisepeda: TextView by lazy {
        findViewById<TextView>(R.id.textjenisepeda)
    }
    private val btnLanjutReview: Button by lazy {
        findViewById<Button>(R.id.btnLanjutReview)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_pesanan)
        val listDurasi = listOf<String>(
            "1", "2", "3", "4", "5"
        )
        ArrayAdapter(this, android.R.layout.simple_spinner_item, listDurasi).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spDurasiPesanan.adapter = adapter
        }
        val intent = intent
        if(!intent.hasExtra("SEPEDAAWAL")) {
            val hasilQR = registerForActivityResult(ScanQRCode()) { result ->
//            val hasilqrintent = Intent(this@VenueDestinationActivity,HasilQRActivity::class.java)
                val hasilqr = when(result) {
                    is QRResult.QRSuccess -> result.content.rawValue
                    QRResult.QRUserCanceled -> "User canceled"
                    QRResult.QRMissingPermission -> "Missing Permission"
                    is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
                }
//            Toast.makeText(this, hasilqr, Toast.LENGTH_SHORT).show()
//                Log.d("SCANQR", hasilqr)
                when(result) {
                    is QRResult.QRUserCanceled -> finish()
                    is QRResult.QRMissingPermission -> finish()
                    is QRResult.QRError -> finish()
                    else -> {}
                }
                sepedaAwal = Sepeda.getSepedaFromID(hasilqr)
                venueAwal = sepedaAwal.venue
                prepareVenue(venueAwal)

            }
        } else {
            if (intent.hasExtra("SEPEDAAWAL")) {
                sepedaAwal = intent.getParcelableExtra<Sepeda>("SEPEDAAWAL")!!
                venueAwal = sepedaAwal.venue
            }
        }

            alamatvenueawal.text = venueAwal.title
            venueTujuan = intent.getParcelableExtra<Venue>("VENUETUJUAN")!!
            alamattujuanvenue.text = venueTujuan.title
            textjenisepeda.text = sepedaAwal.model
        spDurasiPesanan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                durasiPerjalanan = listDurasi[p2].toInt()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        btnLanjutReview.setOnClickListener {
            val intent: Intent = Intent(this, NavigasiActivity::class.java)
            intent.putExtra("PESANAN", Pesanan(sepedaAwal, venueTujuan, durasiPerjalanan))
            startActivity(intent)
            finish()
        }

    }
    private fun prepareVenue(venue: Venue) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading_dialog, null)
        dialogBuilder.setView(dialogView)

        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()
        Toast.makeText(this, venue.title, Toast.LENGTH_SHORT).show()
        alertDialog.dismiss()
    }
}