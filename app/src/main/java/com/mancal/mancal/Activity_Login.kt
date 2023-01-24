package com.mancal.mancal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Activity_Login : AppCompatActivity() {
    private val txtEmailLogin: EditText by lazy {
        findViewById<EditText>(R.id.txtEmailLogin)
    }
    private val txtPasswordLogin: EditText by lazy {
        findViewById<EditText>(R.id.txtPasswordLogin)
    }
    private val btnLogin: Button by lazy {
        findViewById<Button>(R.id.btnLogin)
    }
    private val txtRegisterVBtn: TextView by lazy {
        findViewById<TextView>(R.id.txtRegisterBtn)
    }
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        btnLogin.setOnClickListener {
            auth.signInWithEmailAndPassword(txtEmailLogin.text.toString(), txtPasswordLogin.text.toString())
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        val intent = Intent(this@Activity_Login, Activity_Dashboard::class.java)
                        startActivity(intent)
                        finish()
//                        Toast.makeText(this, auth.currentUser?.uid.toString(), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Login gagal!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        txtRegisterVBtn.setOnClickListener {
            val intent = Intent(this, Activity_Register::class.java)
            startActivity(intent)
        }
    }
}