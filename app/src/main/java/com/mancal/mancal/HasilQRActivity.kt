package com.mancal.mancal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class HasilQRActivity : AppCompatActivity() {
    private lateinit var txtHasilQR: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hasil_qractivity)
        val intent = intent
        txtHasilQR = findViewById(R.id.txtHasilQR)
        txtHasilQR.text = intent.getStringExtra("HASIL")

    }
}