package com.shepeliev.apprtckmm

import androidx.navigation.NavController
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.shepeliev.apprtckmm.databinding.FragmentCallBinding
import com.shepeliev.apprtckmm.shared.call.CallView
import org.webrtc.RendererCommon

class AndroidCallView(
    private val binding: FragmentCallBinding,
    private val navController: NavController,
) : BaseMviView<CallView.Model, CallView.Event>(), CallView {

    init {
        binding.btnHangup.setOnClickListener { dispatch(CallView.Event.HangupClicked) }
        binding.btnSwitchCamera.setOnClickListener { dispatch(CallView.Event.SwitchCameraClicked) }

        with(binding.localVideoPreview) {
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            setMirror(true)
        }

        with(binding.fullScreenVideo) {
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }
    }

    override fun render(model: CallView.Model) {
        super.render(model)
        binding.fullScreenVideo.userMedia = model.remoteUserMedia
        binding.localVideoPreview.userMedia = model.localUserMedia
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}