package com.shepeliev.apprtckmm

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.shepeliev.apprtckmm.databinding.FragmentCallBinding
import com.shepeliev.apprtckmm.shared.call.CallController
import com.shepeliev.apprtckmm.shared.call.CallView

class CallFragment : Fragment(R.layout.fragment_call) {

    private val args: CallFragmentArgs by navArgs()
    private var binding: FragmentCallBinding? = null

    private val controller by lazy {
        CallController(args.roomUrl, args.roomId, lifecycle.asMviLifecycle())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)
        val callView = AndroidCallView(binding!!, findNavController())
        controller.onViewCreated(callView, viewLifecycleOwner.lifecycle.asMviLifecycle())

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            callView.dispatch(CallView.Event.HangupClicked)
        }
    }

    override fun onDestroyView() {
        binding?.localVideoPreview?.release()
        binding?.fullScreenVideo?.release()
        binding = null
        super.onDestroyView()
    }
}
