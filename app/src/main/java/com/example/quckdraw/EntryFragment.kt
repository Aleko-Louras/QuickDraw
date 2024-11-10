package com.example.quckdraw  // Corrected package name

import android.os.Bundle
import android.os.Looper
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

        //Timer for splashscreen navigation
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            navigateToDrawingListFragment()
        }, 2000)

        return binding.root
    }

    /**
     * Helper for navigating to drawing list fragment
     */
    private fun navigateToDrawingListFragment(){
        findNavController().navigate(R.id.action_go_to_drawing_list_fragment)
    }

}
