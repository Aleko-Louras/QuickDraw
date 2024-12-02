package com.example.quckdraw

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.quckdraw.databinding.FragmentCloudDrawingsBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
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
            DrawingListScreen(viewModel)
        }

        return binding.root
    }

}

@Composable
fun DrawingListScreen(viewModel: DrawingViewModel) {
    viewModel.fetchDrawingUrls()
    val drawingUrls by viewModel.drawingUrls.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Uploaded Drawings List"
        )

        if (drawingUrls.isEmpty()) {
            Text(
                text = "No drawings uploaded yet."
            )
        } else {
            LazyColumn {
                items(drawingUrls) { imageUrl ->
                    Log.d("URLS", imageUrl)
                    SharedDrawingItem(imageUrl)
                }
            }
        }
    }
}

@Composable
fun SharedDrawingItem(
    drawingPath: String,  // Path to the image in Firebase Storage
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val storageRef = Firebase.storage.reference

    // Load image asynchronously using a LaunchedEffect
    LaunchedEffect(drawingPath) {
        bitmap = downloadImage(storageRef, drawingPath)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Display the image (if available)
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Drawing Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Adjust size as needed
                    .padding(bottom = 8.dp)
            )
        }
    }
}

suspend fun downloadImage(ref: StorageReference, path: String): Bitmap? {
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


