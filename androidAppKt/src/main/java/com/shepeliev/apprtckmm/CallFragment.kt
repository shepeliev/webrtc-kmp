package com.shepeliev.apprtckmm

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.shepeliev.apprtckmm.databinding.FragmentCallBinding
import com.shepeliev.webrtckmm.android.EglBaseProvider
import org.webrtc.RendererCommon

class CallFragment : Fragment(R.layout.fragment_call) {

    private val args: CallFragmentArgs by navArgs()
    private val vm: CallViewModel by viewModels()
    private lateinit var binding: FragmentCallBinding

    private val eglBase by lazy { EglBaseProvider.getEglBase() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            vm.disconnect()
        }

        binding.btnHangup.setOnClickListener { vm.disconnect() }

        binding.btnSwitchCamera.setOnClickListener { vm.switchCamera() }

        vm.navController = findNavController()

        vm.localSink = binding.localVideoPreview.also {
            it.init(eglBase.eglBaseContext, null)
            it.setEnableHardwareScaler(true)
            it.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            it.setMirror(true)
        }

        vm.remoteSink = binding.fullScreenVideo.also {
            it.init(eglBase.eglBaseContext, null)
            it.setEnableHardwareScaler(true)
            it.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }

        vm.connectToRoom(args.roomUrl, args.roomId)
    }

    override fun onDestroyView() {
        binding.localVideoPreview.release()
        binding.fullScreenVideo.release()
        super.onDestroyView()
    }
}
