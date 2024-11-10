package com.example.quckdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.navigation.fragment.findNavController
import com.example.quckdraw.databinding.FragmentDisplayBinding
import com.example.quckdraw.databinding.FragmentDrawingListViewBinding

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
                    // Handle drawing click, maybe navigate to a detail screen
                },
                onDisplayClick = {
                    findNavController().navigate(R.id.action_go_to_display_fragment)
                }
            )
        }

        return binding.root
    }


}


@Composable
fun DrawingListView(drawings: List<DrawingData>,
                    onDrawingClick: (DrawingData) -> Unit,
                    onDisplayClick: () -> Unit) {
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
                DrawingItem(drawing = drawing, onClick = { onDrawingClick(drawing) })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onDisplayClick,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Display Button")
        }
    }
}

@Composable
fun DrawingItem(drawing: DrawingData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(text = drawing.filename)
    }
}
