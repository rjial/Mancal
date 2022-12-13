package com.mancal.mancal

import android.R.id.home
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.QRResult



class MainActivity : AppCompatActivity() {
    private val waktu_loading = 4000

    //4000=4 detik
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val hasilQR = registerForActivityResult(ScanQRCode()) { result ->
            val hasilqrintent = Intent(this@MainActivity,HasilQRActivity::class.java)
            val hasilqr = when(result) {
                is QRResult.QRSuccess -> result.content.rawValue
                QRResult.QRUserCanceled -> "User canceled"
                QRResult.QRMissingPermission -> "Missing Permission"
                is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
            }
            hasilqrintent.putExtra("HASIL", hasilqr)
            startActivity(hasilqrintent)
            finish()
        }
        Handler().postDelayed({ //setelah loading maka akan langsung berpindah ke home activity
                              hasilQR.launch(null)
//            val home = Intent(this@MainActivity, Activity_Dashboard::class.java)
//            startActivity(home)
//            finish()
        }, waktu_loading.toLong())
    }
}