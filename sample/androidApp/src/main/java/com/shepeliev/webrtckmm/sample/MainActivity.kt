package com.shepeliev.webrtckmm.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val btnUserMedia by lazy { findViewById<MaterialButton>(R.id.btn_user_media_sample) }
    private val btnLoopback by lazy { findViewById<MaterialButton>(R.id.btn_loopback_sample) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnUserMedia.setOnClickListener {
            val intent = Intent(this, UserMediaSampleActivity::class.java)
            startActivity(intent)
        }

        btnLoopback.setOnClickListener {
            val intent = Intent(this, LoopbackSampleActivity::class.java)
            startActivity(intent)
        }
    }
}