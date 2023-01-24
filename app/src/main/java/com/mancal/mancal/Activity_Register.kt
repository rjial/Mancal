package com.mancal.mancal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class Activity_Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val txtNamaRegister: EditText = findViewById<EditText>(R.id.txtNamaRegister)
        val txtEmailRegister: EditText = findViewById<EditText>(R.id.txtEmailRegister)
        val txtPasswordRegister: EditText = findViewById<EditText>(R.id.txtPasswordRegister)
        val btnRegister: Button = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val auth = Firebase.auth
            auth.createUserWithEmailAndPassword(txtEmailRegister.text.toString(), txtPasswordRegister.text.toString())
                .addOnSuccessListener {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = txtNamaRegister.text.toString()
                    }
                    auth.currentUser!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { 
                            if (it.isSuccessful) {
                                auth.signOut()
                                Toast.makeText(this@Activity_Register, "Registrasi Berhasil!", Toast.LENGTH_SHORT)
                                    .show()
                                finish()
//                                startActivity(Intent(this@Activity_Register, ))
                            }
                        }
                }
        }

    }
}