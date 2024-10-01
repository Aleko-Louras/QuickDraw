package com.example.quckdraw  // Corrected package name

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.quckdraw.R
import com.example.quckdraw.databinding.FragmentEntryBinding

class EntryFragment : Fragment() {

    private val viewModel: DrawingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        val binding = FragmentEntryBinding.inflate(inflater, container, false)
        binding.enterDrawingButton.setOnClickListener {
            Log.e("entry frag", "navigating")
            findNavController().navigate(R.id.action_enter_drawing_submitted)
        }

        // Set click listener for the submit button


        Log.e("entry fragment", "here it is")
        return binding.root
    }
}
