package com.rekoj134.filamentdemo

import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.utils.Utils
import com.rekoj134.filamentdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var surfaceView: SurfaceView

    companion object {
        init {
            Utils.init()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.init()
        val renderer = ModelRenderer()
        binding.surfaceView.apply {
            renderer.onSurfaceAvailable(this, lifecycle)
        }
    }
}
