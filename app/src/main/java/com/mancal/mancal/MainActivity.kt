package com.mancal.mancal

import android.R.id.home
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.QRResult
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private val waktu_loading = 4000
    private val activityScope = CoroutineScope(Dispatchers.Main)

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

        activityScope.launch {
            delay(3000)
            val user = Firebase.auth.currentUser
            lateinit var intent: Intent
            if (user != null) {
                intent = Intent(this@MainActivity, Activity_Dashboard::class.java)
            } else {
                intent = Intent(this@MainActivity, Activity_Login::class.java)
            }
            startActivity(intent)
            finish()
        }
//        Handler().postDelayed({ //setelah loading maka akan langsung berpindah ke home activity
////                              hasilQR.launch(null)
////            val home = Intent(this@MainActivity, Activity_Dashboard::class.java)
////            val home = Intent(this@MainActivity, NavigasiActivity::class.java)
//            val login = Intent(this@MainActivity, Activity_Login::class.java)
//            val
////            startActivity(home)
//            startActivity(login)
//            finish()
//        }, waktu_loading.toLong())
    }

    override fun onPause() {
        activityScope.cancel()
        super.onPause()
    }
}