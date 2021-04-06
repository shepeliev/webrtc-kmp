package com.shepeliev.webrtckmm.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.shepeliev.webrtckmm.VideoSinkAdapter
import com.shepeliev.webrtckmm.android.EglBaseProvider
import com.shepeliev.webrtckmm.sample.shared.UserMediaSample
import com.shepeliev.webrtckmm.sample.shared.LocalVideoListener
import org.webrtc.SurfaceViewRenderer

class UserMediaSampleActivity : AppCompatActivity(R.layout.activity_user_media_sample), LocalVideoListener {

    private val tag = "MainActivity"

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            startVideo()
        }

    private val btnStartCamera by lazy { findViewById<MaterialButton>(R.id.btn_start_video) }
    private val btnSwitchCamera by lazy { findViewById<MaterialButton>(R.id.btn_switch_camera) }
    private val btnStopVideo by lazy { findViewById<MaterialButton>(R.id.btn_stop_video) }

    private val videoView by lazy {
        findViewById<SurfaceViewRenderer>(R.id.video).apply {
            init(EglBaseProvider.getEglBase().eglBaseContext, null)
            setEnableHardwareScaler(true)
        }
    }

    private val userMedia by lazy { UserMediaSample(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userMedia.videoRenderer = VideoSinkAdapter(videoView)

        btnStartCamera.setOnClickListener {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        btnSwitchCamera.isEnabled = false
        btnSwitchCamera.setOnClickListener { userMedia.switchCamera() }

        btnStopVideo.setOnClickListener {
            btnSwitchCamera.isEnabled = false
            userMedia.stopVideo()
        }
    }

    override fun onDestroy() {
        userMedia.stopVideo()
        videoView.release()
        super.onDestroy()
    }

    private fun startVideo() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "Camera permission denied")
            return
        }

        btnSwitchCamera.isEnabled = false
        userMedia.startVideo()
    }

    override fun onVideoStarted() {
        btnSwitchCamera.isEnabled = true
    }

    override fun onError(description: String?) {
        Log.e(tag, "Local video error: $description")
    }
}