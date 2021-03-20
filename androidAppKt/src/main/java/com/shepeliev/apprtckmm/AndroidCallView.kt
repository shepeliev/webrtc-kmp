package com.shepeliev.apprtckmm

import androidx.navigation.NavController
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.shepeliev.apprtckmm.databinding.FragmentCallBinding
import com.shepeliev.apprtckmm.shared.call.CallView
import com.shepeliev.webrtckmm.android.EglBaseProvider
import org.webrtc.RendererCommon

class AndroidCallView(
    private val binding: FragmentCallBinding,
    private val navController: NavController,
) : BaseMviView<CallView.Model, CallView.Event>(), CallView {

    init {
        binding.btnHangup.setOnClickListener { dispatch(CallView.Event.HangupClicked) }
        binding.btnSwitchCamera.setOnClickListener { dispatch(CallView.Event.SwitchCameraClicked) }

        with(binding.localVideoPreview) {
            init(EglBaseProvider.getEglBase().eglBaseContext, null)
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            setMirror(true)
        }

        with(binding.fullScreenVideo) {
            init(EglBaseProvider.getEglBase().eglBaseContext, null)
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }
    }

    override fun render(model: CallView.Model) {
        super.render(model)
        model.remoteVideo?.videoTrack()?.native?.addSink(binding.fullScreenVideo)
        model.localVideo?.videoTrack()?.native?.addSink(binding.localVideoPreview)
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}