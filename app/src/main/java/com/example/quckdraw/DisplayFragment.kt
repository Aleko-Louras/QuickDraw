package com.example.quckdraw

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
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

        viewModel.penLiveData.observe(viewLifecycleOwner) { pen ->
            binding.colorPalette.setBackgroundColor(pen.color)
        }

        binding.penSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setPenSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.colorPalette.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_color_picker, null)

            val seekBarRed = dialogView.findViewById<SeekBar>(R.id.seekBarRed)
            val seekBarGreen = dialogView.findViewById<SeekBar>(R.id.seekBarGreen)
            val seekBarBlue = dialogView.findViewById<SeekBar>(R.id.seekBarBlue)

            val initialColor = viewModel.penLiveData.value?.color ?: Color.BLACK
            val initialRed = Color.red(initialColor)
            val initialGreen = Color.green(initialColor)
            val initialBlue = Color.blue(initialColor)

            seekBarRed.progress = initialRed
            seekBarGreen.progress = initialGreen
            seekBarBlue.progress = initialBlue

            var currentColor = initialColor

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(dialogView)
            builder.setTitle("Select Color")
            builder.setPositiveButton("OK") { dialog, which ->
                viewModel.setPenColor(currentColor)
            }
            builder.setNegativeButton("Cancel", null)

            val dialog = builder.create()

            val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val red = seekBarRed.progress
                    val green = seekBarGreen.progress
                    val blue = seekBarBlue.progress
                    currentColor = Color.rgb(red, green, blue)
                    // Optionally, update a preview color in the dialog
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }

            seekBarRed.setOnSeekBarChangeListener(onSeekBarChangeListener)
            seekBarGreen.setOnSeekBarChangeListener(onSeekBarChangeListener)
            seekBarBlue.setOnSeekBarChangeListener(onSeekBarChangeListener)

            dialog.show()
        }

        return binding.root
    }

}
