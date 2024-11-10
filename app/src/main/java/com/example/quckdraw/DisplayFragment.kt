package com.example.quckdraw

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.quckdraw.databinding.FragmentDisplayBinding



class DisplayFragment : Fragment() {
    //initialize xml binding and shared viewmodel among fragments
    private lateinit var binding: FragmentDisplayBinding
    private val viewModel: DrawingViewModel by activityViewModels {
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).drawingRepository)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDisplayBinding.inflate(inflater)
        binding.drawingView.setViewModel(viewModel)

        //observe pen for changing pen color
        viewModel.penLiveData.observe(viewLifecycleOwner) { pen ->
            binding.colorPalette.setBackgroundColor(pen.color)
        }

        //observe bar for changing pen size with slider
        binding.penSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setPenSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        //event listener
        binding.buttonLine.setOnClickListener {
            viewModel.setPenShape(Shape.LINE)
            //viewModel.saveDrawing()
        }
        binding.buttonSquare.setOnClickListener {
            viewModel.setPenShape(Shape.SQUARE)
            //viewModel.saveDrawing()
        }
        binding.buttonTriangle.setOnClickListener {
            viewModel.setPenShape(Shape.TRIANGLE)
            //viewModel.saveDrawing()
        }
        binding.buttonCircle.setOnClickListener {
            viewModel.setPenShape(Shape.CIRCLE)
           // viewModel.saveDrawing()
        }

        //button listener to display user inputted color values, with the color as well
        binding.colorPalette.setOnClickListener {
            //initialize the color slider
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_color_picker, null)
            val colorPreview = dialogView.findViewById<View>(R.id.colorPreview)

            val barRed = dialogView.findViewById<SeekBar>(R.id.seekBarRed)
            val barGreen = dialogView.findViewById<SeekBar>(R.id.seekBarGreen)
            val barBlue = dialogView.findViewById<SeekBar>(R.id.seekBarBlue)

            val initialColor = viewModel.penLiveData.value?.color ?: Color.BLACK
            val initRed = Color.red(initialColor)
            val initGreen = Color.green(initialColor)
            val initBlue = Color.blue(initialColor)

            barRed.progress = initRed
            barGreen.progress = initGreen
            barBlue.progress = initBlue

            var currentColor = initialColor

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(dialogView)
            builder.setTitle("Select Color")
            builder.setPositiveButton("OK") { dialog, which ->
                viewModel.setPenColor(currentColor)
            }
            builder.setNegativeButton("Cancel", null)

            val dialog = builder.create()

            val barChangeListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val red = barRed.progress
                    val green = barGreen.progress
                    val blue = barBlue.progress
                    currentColor = Color.rgb(red, green, blue)
                    colorPreview.setBackgroundColor(currentColor)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }

            barRed.setOnSeekBarChangeListener(barChangeListener)
            barGreen.setOnSeekBarChangeListener(barChangeListener)
            barBlue.setOnSeekBarChangeListener(barChangeListener)

            dialog.show()
        }

        binding.viewDrawingsButton.setOnClickListener{
            findNavController().navigate(R.id.action_back_to_drawing_list_fragment)
        }

        return binding.root
    }


}
