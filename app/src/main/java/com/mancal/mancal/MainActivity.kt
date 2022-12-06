package com.mancal.mancal

import android.R.id.home
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val waktu_loading = 4000

    //4000=4 detik
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler().postDelayed({ //setelah loading maka akan langsung berpindah ke home activity
            val home = Intent(this@MainActivity, Activity_Dashboard::class.java)
            startActivity(home)
            finish()
        }, waktu_loading.toLong())
    }
}