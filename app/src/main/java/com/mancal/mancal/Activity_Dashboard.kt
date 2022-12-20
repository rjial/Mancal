package com.mancal.mancal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class Activity_Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
    }
    data class DataItem(var image: String?, var title: String?, var date: String?, var link: String?)
}