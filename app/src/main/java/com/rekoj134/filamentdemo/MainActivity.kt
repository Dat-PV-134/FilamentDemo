package com.rekoj134.filamentdemo

import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.utils.Utils

class MainActivity : AppCompatActivity() {
    private lateinit var surfaceView: SurfaceView

    companion object {
        init {
            Utils.init()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.init()
        val renderer = ModelRenderer()
        surfaceView = SurfaceView(this).apply {
            renderer.onSurfaceAvailable(this, lifecycle)
        }
        setContentView(surfaceView)
    }
}
