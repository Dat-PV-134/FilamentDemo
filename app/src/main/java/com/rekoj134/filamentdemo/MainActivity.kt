package com.rekoj134.filamentdemo

import android.Manifest
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.filament.utils.Utils
import com.rekoj134.filamentdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var surfaceView: SurfaceView

    companion object {
        init {
            Utils.init()
        }

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.init()
        val renderer = ModelRenderer()
        binding.surfaceView.apply {
            renderer.onSurfaceAvailable(this, lifecycle)
        }
        binding.joystick.listener = object : JoystickView.OnJoystickMoveListener {
            override fun onMove(x: Float, y: Float, angle: Float, strength: Float) {
                renderer.setMoveDirection(x, -y)
            }

            override fun onRelease() {
                renderer.setMoveDirection(0f, 0f)
            }
        }
        requestPermissions()
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (permissionGranted) {
                setupCamera()
            }
        }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.viewFinder.surfaceProvider
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this,
                    "Camera binding failed: ${exc.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        }, ContextCompat.getMainExecutor(this))
    }
}
