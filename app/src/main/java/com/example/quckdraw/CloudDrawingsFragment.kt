package com.example.quckdraw

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.quckdraw.databinding.FragmentCloudDrawingsBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class CloudDrawingsFragment : Fragment() {

    private val viewModel: DrawingViewModel by activityViewModels {
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).drawingRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentCloudDrawingsBinding.inflate(layoutInflater)
        binding.composeView2.setContent {

            DrawingListScreen(viewModel, findNavController())
        }
        return binding.root
    }

    /**
     * Helper function to show the dialog for creating a new drawing
     */
    private fun showCreateNewDrawingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter drawing name")

        val input = EditText(requireContext())
        input.hint = "Drawing name"
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, _ ->
            val drawingName = input.text.toString()
            if (drawingName.isNotBlank()) {
                // Create a new drawing with the specified name and save it
                viewModel.viewModelScope.launch {
                    if (viewModel.createNewDrawing(drawingName)) {
                        //after the name is confirmed then navigate to display fragment
                        findNavController().navigate(R.id.action_go_to_display_fragment)
                    } else {
                        Toast.makeText(context, "A drawing with this name already exists.", Toast.LENGTH_SHORT).show()
                    }
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
fun DrawingListScreen(viewModel: DrawingViewModel, navController: NavController) {
    viewModel.fetchDrawingUrls()
    val drawingUrls by viewModel.drawingUrls.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Button(modifier = Modifier, onClick = {navController.navigate(R.id.action_back_to_drawing_list_fragment)}){
            Text(text = "Back")
        }
        Text(
            text = "Shared Drawings",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        if (drawingUrls.isEmpty()) {
            Text(
                text = "No drawings uploaded yet."
            )
        } else {
            LazyColumn {
                items(drawingUrls) { imageUrl ->
                    Log.d("URLS", imageUrl)
                    SharedDrawingItem(imageUrl, viewModel)
                }
            }
        }
    }
}

@Composable
fun SharedDrawingItem(
    drawingPath: String,  // Path to the image in Firebase Storage
    viewModel: DrawingViewModel
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load image asynchronously using a LaunchedEffect
    LaunchedEffect(drawingPath) {
        bitmap = downloadImage(drawingPath)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(color = androidx.compose.ui.graphics.Color.Black)
    ) {
        var text by remember { mutableStateOf("") }


        // Display the image (if available)
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Drawing Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Adjust size as needed
                    .padding(bottom = 8.dp, top = 8.dp)
            )
            Button(
                onClick = {
                    viewModel.viewModelScope.launch {
                        val success = viewModel.createNewCloudDrawing(text, it)
                        val message = if (success) {
                            "Drawing downloaded"
                        } else {
                            "Failed to download the drawing"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ){
                Text(text = "Download")
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),

                value = text,
                onValueChange = { text = it },
                label = { Text("Save Drawing as") })
        }
    }

}

suspend fun downloadImage(path: String): Bitmap? {
    val fileRef = Firebase.storage.getReferenceFromUrl(path)
    return suspendCoroutine { continuation ->
        fileRef.getBytes(10 * 1024 * 1024) // 10 MB max size
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                continuation.resume(bitmap)
            }
            .addOnFailureListener { e ->
                Log.e("DOWNLOAD_IMAGE", "Failed to get image $e")
                continuation.resume(null)
            }
    }
}

