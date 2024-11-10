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

        viewModel.viewDisplayFragment.observeForever {
            if (it) {
                navigateToDrawFragment()
                viewModel.setViewDisplayFragment(false) // Reset to prevent repeated navigation
            }
        }

        viewModel.viewDrawingsFragment.observeForever {
            if (it) {
                navigateToDrawingListFragment()
                viewModel.setViewDrawingsFragment(false)
            }
        }
        return binding.root
    }

    /**
     * Helper for navigating to draw fragment
     */
    private fun navigateToDrawFragment() {
        Log.e("entry frag", "navigating to list view")
        findNavController().navigate(R.id.action_go_to_display_fragment)
    }
    /**
     * Helper for navigating to drawing list fragment
     */
    private fun navigateToDrawingListFragment(){
        findNavController().navigate(R.id.action_go_to_drawing_list_fragment)
    }
    /**
     * Clean up observers
     */
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.viewDisplayFragment.removeObserver {  }
    }
}
