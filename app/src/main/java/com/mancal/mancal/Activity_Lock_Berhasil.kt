package com.mancal.mancal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.mancal.mancal.model.Invoice

class Activity_Lock_Berhasil : AppCompatActivity() {
    private lateinit var invoice: Invoice
    private val btnConfirmQR: Button by lazy {
        findViewById<Button>(R.id.btnConfirmQR)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_berhasil)
        if (intent.hasExtra("INVOICE")) {
            invoice = intent.getParcelableExtra<Invoice>("INVOICE")!!
        } else {
            finish()
        }
        val intentInvoice = Intent(this, PrePaymentActivity::class.java)
        btnConfirmQR.setOnClickListener {

            intentInvoice.putExtra("INVOICE", invoice)
            startActivity(intentInvoice)
            finish()
        }
    }
}