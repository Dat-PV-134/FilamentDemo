package com.rekoj134.filamentdemo

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.view.Choreographer
import android.view.SurfaceView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.ModelViewer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ModelRenderer {
    private lateinit var surfaceView: SurfaceView
    private lateinit var lifecycle: Lifecycle

    private lateinit var choreographer: Choreographer
    private lateinit var uiHelper: UiHelper

    private lateinit var modelViewer: ModelViewer

    private val assets: AssetManager
        get() = surfaceView.context.assets

    private val frameScheduler = FrameCallback()

    private var headlightEntity: Int = 0

    private var moveDirX = 0f
    private var moveDirY = 0f
    private var posX = 0f
    private var posY = 0f

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            choreographer.postFrameCallback(frameScheduler)
        }

        override fun onPause(owner: LifecycleOwner) {
            choreographer.removeFrameCallback(frameScheduler)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            choreographer.removeFrameCallback(frameScheduler)
            lifecycle.removeObserver(this)
        }
    }

    private var lastAngleDeg = 0f

    @SuppressLint("ClickableViewAccessibility")
    fun onSurfaceAvailable(surfaceView: SurfaceView, lifecycle: Lifecycle) {
        this.surfaceView = surfaceView
        this.lifecycle = lifecycle

        lifecycle.addObserver(lifecycleObserver)

        choreographer = Choreographer.getInstance()
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            isOpaque = false
        }

        modelViewer = ModelViewer(surfaceView = surfaceView, uiHelper = uiHelper)

        surfaceView.setOnTouchListener { _, event ->
            true
        }

        modelViewer.scene.skybox = null
        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
        modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
            clear = true
        }

        modelViewer.view.renderQuality = modelViewer.view.renderQuality.apply {
            hdrColorBuffer = View.QualityLevel.LOW
        }

        createRenderables()
    }

    private fun createRenderables() {
        val buffer = assets.open("urotsuki.glb").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.allocateDirect(bytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(bytes)
                rewind()
            }
        }

        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()

        val entityManager = EntityManager.get()
        headlightEntity = entityManager.create()

        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(100_000.0f)
            .direction(0.0f, 0.0f, -1.0f)
            .castShadows(false)
            .build(modelViewer.engine, headlightEntity)

        modelViewer.scene.addEntity(headlightEntity)
    }

    inner class FrameCallback : Choreographer.FrameCallback {
        var startTime = System.nanoTime()

        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)

            // Cập nhật hướng đèn theo camera
            val cam = modelViewer.camera
            val look = FloatArray(3)
            cam.getForwardVector(look)
            modelViewer.engine.lightManager.setDirection(
                modelViewer.engine.lightManager.getInstance(headlightEntity),
                look[0], look[1], look[2]
            )

            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
                    applyAnimation(2, elapsedTimeSeconds.toFloat())
                }
                updateBoneMatrices()
            }

            val speed = 0.015f // tốc độ bước mỗi frame
            posX += moveDirX * speed
            posY += moveDirY * speed

            // Góc quay đầu theo hướng đi
            val angle = if (moveDirX != 0f || moveDirY != 0f) {
                Math.toDegrees(kotlin.math.atan2(moveDirX, -moveDirY).toDouble()).toFloat()
            } else 0f

            val transformMatrix = FloatArray(16)
            android.opengl.Matrix.setIdentityM(transformMatrix, 0)

            // Dịch model xuống sàn và lùi ra xa
            android.opengl.Matrix.translateM(transformMatrix, 0, 0f, -1.0f, -2.5f)

            val local = FloatArray(16)
            android.opengl.Matrix.setIdentityM(local, 0)

            // Scale động dựa trên khoảng cách theo trục Y
            val baseScale = 1f
            val scaleFactor = 1f - (posY * 0.3f).coerceIn(-0.5f, 0.5f)
            val finalScale = baseScale * scaleFactor
            android.opengl.Matrix.scaleM(local, 0, finalScale, finalScale, finalScale)

            android.opengl.Matrix.translateM(local, 0, posX, posY, 0f)
            android.opengl.Matrix.rotateM(local, 0, angle, 0f, 1f, 0f)

            android.opengl.Matrix.multiplyMM(transformMatrix, 0, transformMatrix, 0, local, 0)

            modelViewer.asset?.root?.let {
                val tcm = modelViewer.engine.transformManager
                val inst = tcm.getInstance(it)
                tcm.setTransform(inst, transformMatrix)
            }

            modelViewer.render(frameTimeNanos)
        }
    }

    fun setMoveDirection(x: Float, y: Float) {
        moveDirX = x
        moveDirY = y
    }
}
