package com.shepeliev.apprtckmm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shepeliev.apprtckmm.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWelcomeBinding.bind(view)

        binding.btnJoinRoom.setOnClickListener {
            val roomId = binding.editTextRoomName.text
                ?.takeIf(CharSequence::isNotEmpty)
                ?.toString()
                ?: return@setOnClickListener

            val action = WelcomeFragmentDirections.actionWelcomeFragmentToCallFragment(
                getString(R.string.pref_default_room_url),
                roomId
            )
            findNavController().navigate(action)
        }
    }
}
