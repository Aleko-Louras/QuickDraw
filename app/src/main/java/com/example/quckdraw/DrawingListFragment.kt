package com.example.quckdraw

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.quckdraw.databinding.FragmentDisplayBinding
import com.example.quckdraw.databinding.FragmentDrawingListViewBinding
import kotlinx.coroutines.launch

class DrawingListFragment : Fragment() {

    private val viewModel: DrawingViewModel by activityViewModels {
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).drawingRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentDrawingListViewBinding.inflate(layoutInflater)

        binding.composeView1.setContent {
            val drawings by viewModel.drawingsList.observeAsState(emptyList())
            DrawingListView(
                drawings = drawings,
                onDrawingClick = { drawing ->
                    viewModel.loadDrawing(drawing.filename)

                    // Navigate to the display fragment
                    findNavController().navigate(R.id.action_go_to_display_fragment)
                },
                onDeleteClick = { drawing ->
                    // Delete the selected drawing
                    viewModel.viewModelScope.launch {
                        viewModel.deleteDrawing(drawing)
                    }
                },
                onCreateNewDrawingClick = {
                    showCreateNewDrawingDialog()
                }
            )
        }

        return binding.root
    }
    private fun showCreateNewDrawingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter drawing name")

        val input = EditText(requireContext())
        input.hint = "Drawing name"
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, _ ->
            val drawingName = input.text.toString()
            if (drawingName.isNotBlank()) {
                // Create a new drawing with the specified name
                viewModel.viewModelScope.launch {
                    viewModel.CreateNewDraw(drawingName)
                    findNavController().navigate(R.id.action_go_to_display_fragment)
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }


}

@Composable
fun DrawingListView(
    drawings: List<DrawingData>,
    onDrawingClick: (DrawingData) -> Unit,
    onDeleteClick: (DrawingData) -> Unit,
    onCreateNewDrawingClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
        ) {
            items(drawings) { drawing ->
                DrawingItem(
                    drawing = drawing,
                    onClick = { onDrawingClick(drawing) },
                    onDeleteClick = { onDeleteClick(drawing) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onCreateNewDrawingClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Drawing")
        }
    }
}


@Composable
fun DrawingItem(
    drawing: DrawingData,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = drawing.filename,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )

        Button(onClick = onDeleteClick) {
            Text("Delete")
        }
    }
}

