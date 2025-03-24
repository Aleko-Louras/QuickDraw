package com.example.quckdraw

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.quckdraw.databinding.FragmentDrawingListViewBinding
import com.google.firebase.auth.FirebaseAuth

import kotlinx.coroutines.launch

class DrawingListFragment : Fragment() {

    private val viewModel: DrawingViewModel by activityViewModels {
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).drawingRepository)
    }
    private lateinit var bluetoothService: BluetoothService


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bluetoothService = BluetoothService(requireContext())
        // Inflate the layout for this fragment
        val binding = FragmentDrawingListViewBinding.inflate(layoutInflater)

        binding.composeView1.setContent {
            //initialize the drawings to observe as state, and recompose them in the drawing list view.
            //also pass in the lambda functions for each click listener
            val drawings by viewModel.drawingsList.observeAsState(emptyList())
            DrawingListScreen(
                drawings = drawings,
                bluetoothService = bluetoothService,
                onCreateNewDrawingClick = { showCreateNewDrawingDialog() },
                onSignOutUser = {
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_go_to_auth_fragment)
                },
                onSharedClick = {
                    findNavController().navigate(R.id.action_drawingListFragment_to_cloudDrawingsFragment)
                },
                onDrawingClick = { drawing ->
                    viewModel.loadDrawing(drawing.filename)
                    findNavController().navigate(R.id.action_go_to_display_fragment)
                },
                onDeleteClick = { drawing ->
                    viewModel.viewModelScope.launch {
                        viewModel.deleteDrawing(drawing)
                    }
                },
                onUploadClick = { drawing ->
                    viewModel.viewModelScope.launch {
                        viewModel.loadDrawing(drawing.filename)
                        viewModel.uploadDrawing(drawing)
                    }
                    Toast.makeText(context, "Drawing uploaded", Toast.LENGTH_SHORT).show()
                }
            )
//            DrawingListView(
//                drawings = drawings,
//                onDrawingClick = { drawing ->
//                    viewModel.loadDrawing(drawing.filename)
//                    // Navigate to the display fragment
//                    findNavController().navigate(R.id.action_go_to_display_fragment)
//                },
//                onDeleteClick = { drawing ->
//                    // Delete the selected drawing
//                    viewModel.viewModelScope.launch {
//                        viewModel.deleteDrawing(drawing)
//                    }
//                },
//                onUploadClick = { drawing ->
//                    viewModel.viewModelScope.launch {
//                        viewModel.loadDrawing(drawing.filename)
//                        viewModel.uploadDrawing(drawing)
//                    }
//                    Toast.makeText(context, "Drawing uploaded", Toast.LENGTH_SHORT).show()
//
//                },
//                onCreateNewDrawingClick = {
//                    showCreateNewDrawingDialog()
//                },
//                onSignOutUser = {
//                    FirebaseAuth.getInstance().signOut()
//                    findNavController().navigate(R.id.action_go_to_auth_fragment)
//                },
//                onSharedClick = {
//                    findNavController().navigate((R.id.action_drawingListFragment_to_cloudDrawingsFragment))
//                },
//                onShareBTDrawing = { drawing ->
//                   scanForAvailableDevices()
//                    }
//            )
        }

        return binding.root
    }
    /**
     * Initiates scanning for available Bluetooth devices and shows a dialog with the results.
     */
    @SuppressLint("MissingPermission")
    private fun scanForAvailableDevices() {
        Log.e("BluetoothService", "Scanning for available devices...")
        bluetoothService.scanForDevices { devices ->
            // Build a list of device names
            Log.e("BluetoothService", "Hi")
            val deviceNames = devices.map {
                if (!it.name.isNullOrEmpty()) it.name else "Unknown Device"
            }.toTypedArray()
            if (deviceNames.isEmpty()) {

                Toast.makeText(context, "No devices found.", Toast.LENGTH_SHORT).show()
            } else {
                // Show a dialog with the discovered devices
                AlertDialog.Builder(requireContext())
                    .setTitle("Select device")
                    .setItems(deviceNames) { _, which ->
                        val selectedDevice = devices[which]
                        Toast.makeText(context, "Selected: ${selectedDevice.name}", Toast.LENGTH_SHORT).show()
                        // Here you could call bluetoothService.connectToDevice(selectedDevice) if desired.
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
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

/**
 * Composable for displaying a list of drawings. Takes in the list of drawings, a lambda for when a drawing is clicked,
 * a lambda for when a delete button is clicked, and a lambda for when the "Create New Drawing" button is clicked. Utilizes
 * a lazy column as a recycler view with a single button to create a new drawing.
 */
@Composable
fun DrawingListView(
    drawings: List<DrawingData>,
    onDrawingClick: (DrawingData) -> Unit,
    onDeleteClick: (DrawingData) -> Unit,
    onUploadClick: (DrawingData) -> Unit,
    onCreateNewDrawingClick: () -> Unit,
    onSignOutUser: () -> Unit,
    onSharedClick: () -> Unit,
    onShareBTDrawing: (DrawingData) -> Unit, // new!
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
                    onDeleteClick = { onDeleteClick(drawing) },
                    onUploadClick = { onUploadClick(drawing)},
                    onShareBTTClick = {
                        // share the drawing here via Bluetooth
                        onShareBTDrawing(drawing)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onCreateNewDrawingClick,
            modifier = Modifier.fillMaxWidth()) {
            Text("Create New Drawing")
        }

        Button(
            onClick = onSharedClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()) {
            Text("Shared drawings")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onSignOutUser,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()) {
            Text("Sign Out")
        }
    }
}

/**
 * Composable for displaying a single drawing item in the list. Displays a  name and delete button for each item.
 */
@Composable
fun DrawingItem(
    drawing: DrawingData,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onUploadClick: () -> Unit,
    onShareBTTClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = drawing.filename,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), // Space between text and buttons
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onUploadClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Upload to Cloud",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload", color = Color.White)
            }

            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", color = Color.White)
            }
            Button(
                onClick = onShareBTTClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // greenish
                modifier = Modifier.height(36.dp)
            ) {
                Text("Share", color = Color.White)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DevicePickerDialog(
    devices: List<BluetoothDevice>,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a device") },
        text = {
            LazyColumn {
                items(devices) { device ->
                    Button(
                        onClick = { onDeviceSelected(device) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // Show device name if available; otherwise "Unknown Device"
                        Text(text = device.name ?: "Unknown Device")
                    }
                }
            }
        },
        confirmButton = { /* No confirm action needed; leave it empty */ },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@SuppressLint("MissingPermission")
@Composable
fun DrawingListScreen(
    drawings: List<DrawingData>,
    bluetoothService: BluetoothService,
    onCreateNewDrawingClick: () -> Unit,
    onSignOutUser: () -> Unit,
    onSharedClick: () -> Unit,
    onDrawingClick: (DrawingData) -> Unit,
    onDeleteClick: (DrawingData) -> Unit,
    onUploadClick: (DrawingData) -> Unit
) {
    val context = LocalContext.current
    // State: discovered devices and whether to show the picker
    var discoveredDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    var showDevicePicker by remember { mutableStateOf(false) }

    // The "share" action triggers a Bluetooth scan
    val onShareBTDrawing: (DrawingData) -> Unit = { drawing ->
        // Start scanning
        bluetoothService.scanForDevices { foundDevices ->
            discoveredDevices = foundDevices
            showDevicePicker = true
        }
    }

    // 1. Show the main drawing list
    DrawingListView(
        drawings = drawings,
        onDrawingClick = onDrawingClick,
        onDeleteClick = onDeleteClick,
        onUploadClick = onUploadClick,
        onCreateNewDrawingClick = onCreateNewDrawingClick,
        onSignOutUser = onSignOutUser,
        onSharedClick = onSharedClick,
        onShareBTDrawing = onShareBTDrawing
    )

    // 2. If user scanned and we have devices, show the Compose device picker dialog
    if (showDevicePicker) {
        DevicePickerDialog(
            devices = discoveredDevices,
            onDeviceSelected = { device ->
                // Optionally connect here
                bluetoothService.connectToDevice(device)
                Toast.makeText(context, "Selected: ${device.name}", Toast.LENGTH_SHORT).show()
                showDevicePicker = false
            },
            onDismiss = {
                showDevicePicker = false
            }
        )
    }
}





