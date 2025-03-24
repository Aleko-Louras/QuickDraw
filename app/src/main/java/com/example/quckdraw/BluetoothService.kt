package com.example.quckdraw

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    private val appUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var serverThread: AcceptThread? = null
    private var clientThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    // (Existing listener for received drawings, if needed)
    private var onDrawingReceived: ((android.graphics.Bitmap) -> Unit)? = null

    fun setOnDrawingReceivedListener(listener: (android.graphics.Bitmap) -> Unit) {
        onDrawingReceived = listener
    }

    fun startServer() {
        serverThread = AcceptThread()
        serverThread?.start()
    }

    fun connectToDevice(device: BluetoothDevice) {
        clientThread = ConnectThread(device)
        clientThread?.start()
    }

    fun sendData(data: ByteArray) {
        if (connectedThread == null) {
            Log.e("BluetoothService", "No connection available. Cannot send data.")
            return
        }
        connectedThread?.write(data)
    }

    // --------------------------
    // NEW: Scanning for available devices
    // --------------------------
    @SuppressLint("MissingPermission")
    fun scanForDevices(onScanResult: (List<BluetoothDevice>) -> Unit) {
        Log.d("BluetoothService", "Starting device discovery")
        val foundDevices = mutableSetOf<BluetoothDevice>()
        val receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        device?.let { foundDevices.add(it)
                        Log.d("BluetoothService", "Found device: ${it.name} - ${it.address}")}
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.d("BluetoothService", "Discovery finished, found ${foundDevices.size} devices")
                        onScanResult(foundDevices.toList())
                        try {
                            context?.unregisterReceiver(this)
                        } catch (e: IllegalArgumentException) {
                            Log.e("BluetoothService", "Receiver not registered", e)
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)
        val discoveryStarted = bluetoothAdapter?.startDiscovery()

    }
    // --------------------------

    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {
        private val serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("DrawingApp", appUUID)

        override fun run() {
            var socket: android.bluetooth.BluetoothSocket?
            while (true) {
                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    break
                }
                if (socket != null) {
                    connectedThread = ConnectedThread(socket)
                    connectedThread?.start()
                    try {
                        serverSocket?.close()
                    } catch (e: IOException) {
                        Log.e("BluetoothService", "Error closing server socket", e)
                    }
                    break
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val socket = device.createRfcommSocketToServiceRecord(appUUID)

        override fun run() {
            bluetoothAdapter?.cancelDiscovery()
            try {
                socket?.connect()
                connectedThread = ConnectedThread(socket!!)
                connectedThread?.start()
            } catch (e: IOException) {
                try {
                    socket?.close()
                } catch (e2: IOException) {
                    Log.e("BluetoothService", "Could not close connection", e2)
                }
            }
        }
    }

    private inner class ConnectedThread(private val socket: android.bluetooth.BluetoothSocket) : Thread() {
        private val inputStream: InputStream = socket.inputStream
        private val outputStream: OutputStream = socket.outputStream

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val receivedData = buffer.copyOf(bytes)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(receivedData, 0, receivedData.size)
                        if (bitmap != null) {
                            onDrawingReceived?.invoke(bitmap)
                        } else {
                            Log.e("BluetoothService", "Failed to decode bitmap from received data")
                        }
                    }
                } catch (e: IOException) {
                    Log.e("BluetoothService", "Error reading input stream", e)
                    break
                }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outputStream.write(bytes)
            } catch (e: IOException) {
                Log.e("BluetoothService", "Error sending data", e)
            }
        }
    }
}