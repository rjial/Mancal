package com.mancal.mancal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mancal.mancal.model.Invoice

class PrePaymentActivity : AppCompatActivity() {
    private lateinit var invoice: Invoice
    private val alamatvenueawalPrePayment: TextView by lazy {
        findViewById(R.id.alamatvenueawalPrePayment)
    }
    private val alamattujuanvenuePrePayment: TextView by lazy {
        findViewById(R.id.alamattujuanvenuePrePayment)
    }
    private val textjenisepedaPrePayment: TextView by lazy {
        findViewById(R.id.textjenisepedaPrePayment)
    }
    private val txtDurasiPrePayment: TextView by lazy {
        findViewById(R.id.txtDurasiPrePayment)
    }
    private val hargaPrePayment: TextView by lazy {
        findViewById(R.id.hargaPrePayment)
    }

    private val btnLanjutPayment: TextView by lazy {
        findViewById(R.id.btnLanjutPayment)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_payment)
        if (intent.hasExtra("INVOICE")) {
            invoice = intent.getParcelableExtra<Invoice>("INVOICE")!!
            alamatvenueawalPrePayment.text = invoice.sepeda.venue.title
            alamattujuanvenuePrePayment.text = invoice.venue.title
            textjenisepedaPrePayment.text = invoice.sepeda.model
            txtDurasiPrePayment.text = invoice.durasi
            hargaPrePayment.text = "Rp " + invoice.totalBayar.toString()
            btnLanjutPayment.setOnClickListener {
                val db = Firebase.firestore
                val sepedaReference: DocumentReference = db.collection("sepeda").document(invoice.sepeda.id)
                val venueReference: DocumentReference = db.collection("venue").document(invoice.venue.id)
                val insertInvoice = db.collection("invoice").add(hashMapOf(
                    "sepeda" to sepedaReference,
                    "venueTujuan" to venueReference,
                    "totalBayar" to invoice.totalBayar,
                    "durasi" to invoice.durasi
                ))
                insertInvoice.addOnSuccessListener {
                    finish()
                }
                insertInvoice.addOnFailureListener {
                    finish()
                }
            }
        } else {
            finish()
        }
    }
}