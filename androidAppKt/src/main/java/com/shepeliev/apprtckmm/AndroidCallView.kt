package com.shepeliev.apprtckmm

import androidx.navigation.NavController
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.shepeliev.apprtckmm.databinding.FragmentCallBinding
import com.shepeliev.apprtckmm.shared.call.CallView
import com.shepeliev.webrtckmm.VideoSinkAdapter
import com.shepeliev.webrtckmm.android.EglBaseProvider
import org.webrtc.RendererCommon

class AndroidCallView(
    private val binding: FragmentCallBinding,
    private val navController: NavController,
) : BaseMviView<CallView.Model, CallView.Event>(), CallView {

    private val eglBase by lazy { EglBaseProvider.getEglBase() }

    init {
        binding.btnHangup.setOnClickListener { dispatch(CallView.Event.HangupClicked) }
        binding.btnSwitchCamera.setOnClickListener { dispatch(CallView.Event.SwitchCameraClicked) }

        with(binding.localVideoPreview) {
            init(eglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            setMirror(true)
        }

        with(binding.fullScreenVideo) {
            init(eglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }
    }

    override fun render(model: CallView.Model) {
        super.render(model)
        model.localVideoTrack?.addSink(VideoSinkAdapter(binding.localVideoPreview))
        model.remoteVideoTrack?.addSink(VideoSinkAdapter(binding.fullScreenVideo))
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}