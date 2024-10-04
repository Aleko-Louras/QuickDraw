package com.example.quckdraw

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.quckdraw.databinding.FragmentDisplayBinding

class DisplayFragment : Fragment() {

    private lateinit var binding: FragmentDisplayBinding
    private val viewModel: DrawingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDisplayBinding.inflate(inflater)
        binding.drawingView.setViewModel(viewModel)

        viewModel.penLiveData.observe(viewLifecycleOwner) {pen ->
            binding.colorPalette.setBackgroundColor(pen.color)
        }

        // Handle Pen Size Change
        binding.penSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setPenSize(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Handle Color Buttons
        binding.buttonBlue.setOnClickListener {
            viewModel.setPenColor(Color.BLUE)
        }

        binding.buttonRed.setOnClickListener {
            viewModel.setPenColor(Color.RED)
        }

        binding.buttonGreen.setOnClickListener {
           viewModel.setPenColor(Color.GREEN)
        }

        return binding.root
    }

}
