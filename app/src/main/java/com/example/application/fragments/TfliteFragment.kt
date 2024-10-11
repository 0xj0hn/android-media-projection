package com.example.application.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageAnalysis.OutputImageFormat
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import androidx.core.view.drawToBitmap
import com.example.application.R
import com.example.application.databinding.FragmentTfliteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.Permission
import kotlin.random.Random

class TfliteFragment : Fragment() {

    companion object {
        fun newInstance() = TfliteFragment()
    }

    private val viewModel: TfliteViewModel by viewModels()
    private var _binding: FragmentTfliteBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    private val activityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data!!.data!!
            var picturePath: String?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val inputStream = BufferedInputStream(requireActivity().contentResolver.openInputStream(uri))
                val inferenceResult = runInference(BitmapFactory.decodeStream(inputStream))
                val resultImage = convertOutputArrayToImage(inferenceResult)
                binding.imageView.setImageBitmap(resultImage)
            }
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTfliteBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        val stream = requireActivity().application.assets.open("1.tflite")
        val bufferedReaderStream = stream.bufferedReader()
        createInterpreter()

        binding.button.setOnClickListener {
            activityForResult.launch(
                ActivityResultContracts.GetContent()
                    .createIntent(requireActivity(), "image/*"))
        }
        requestPermissions()

        return view
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 100)
        }else{
            startCamera()
        }
    }

    private lateinit var bitmapBuffer: Bitmap


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try{
                cameraProvider.unbindAll()
                val imageAnalysis = ImageAnalysis.Builder().apply {
                    setTargetResolution(Size(224, 224))
                    setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                }.build()
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireActivity())
                ) {image ->
                    if (!::bitmapBuffer.isInitialized) {
                        bitmapBuffer = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                    }
                    bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        val resultBytes = runInference(bitmapBuffer)
                        val resultImage = convertOutputArrayToImage(resultBytes)
                        withContext(Dispatchers.Main) {
                            binding.imageView.setImageBitmap(resultImage)
                        }
                    }
                    image.close()
                }
                cameraProvider.bindToLifecycle(requireActivity(), cameraSelector, imageAnalysis)

            }catch(e: Exception){
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }


    private var interpreter: Interpreter? = null

    private fun createInterpreter() {
        val tfLiteOptions = Interpreter.Options().addDelegate(NnApiDelegate()).setNumThreads(1)
        interpreter = Interpreter(FileUtil.loadMappedFile(requireActivity(), "1.tflite"), tfLiteOptions)
    }

    private fun getInputImage(width: Int, height: Int): ByteBuffer {
        val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
        inputImage.order(ByteOrder.nativeOrder())
        inputImage.rewind()
        return inputImage
    }

    private fun convertBitmapToByteBuffer(bitmapIn: Bitmap, width: Int, height: Int): ByteBuffer {
        val bitmap = Bitmap.createScaledBitmap(bitmapIn, width, height, false) // convert bitmap into required size
        // these value can be different for each channel if they are not then you may have single value instead of an array
        val mean = arrayOf(127.5f, 127.5f, 127.5f)
        val standard = arrayOf(127.5f, 127.5f, 127.5f)
        val inputImage = getInputImage(width, height)
        val intValues = IntArray(width * height)
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
        for (y in 0 until width) {
            for (x in 0 until height) {
                val px = bitmap.getPixel(x, y)                // Get channel values from the pixel value.
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)                // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                // For example, some models might require values to be normalized to the range
                // [0.0, 1.0] instead.
                val rf = (r - mean[0]) / standard[0]
                val gf = (g - mean[0]) / standard[0]
                val bf = (b - mean[0]) / standard[0]                //putting in BRG order because this model demands input in this order
                inputImage.putFloat(bf)
                inputImage.putFloat(rf)
                inputImage.putFloat(gf)
            }
        }
        return inputImage
    }

    private fun runInference(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val outputArr = Array(1) {
            Array(224) {
                Array(224) {
                    FloatArray(3)
                }
            }
        }
        val byteBuffer = convertBitmapToByteBuffer(bitmap, 224, 224)
        interpreter?.run(byteBuffer, outputArr)
        return outputArr
    }

    private fun convertOutputArrayToImage(inferenceResult: Array<Array<Array<FloatArray>>>): Bitmap {
        val output = inferenceResult[0]
        val bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(224 * 224)

        var index = 0
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val b = (output[y][x][0] + 1) * 127.5
                val r = (output[y][x][1] + 1) * 127.5
                val g = (output[y][x][2] + 1) * 127.5
                val a = 0xFF
                pixels[index] = a shl 24 or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
                index++
            }
        }
        bitmap.setPixels(pixels, 0, 224, 0, 0, 224, 224)
        return bitmap
    }
}