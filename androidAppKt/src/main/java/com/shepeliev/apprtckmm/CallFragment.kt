package com.shepeliev.apprtckmm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs

class CallFragment : Fragment(R.layout.fragment_call) {

    private val args: CallFragmentArgs by navArgs()
    private val vm: CallViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.connectToRoom(args.roomUrl, args.roomId)
    }
}
