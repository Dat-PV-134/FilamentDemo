package com.rekoj134.filamentdemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class JoystickView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    interface OnJoystickMoveListener {
        fun onMove(x: Float, y: Float, angle: Float, strength: Float)
        fun onRelease()
    }

    var listener: OnJoystickMoveListener? = null

    private val bgPaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
        alpha = 80
        isAntiAlias = true
    }

    private val stickPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var stickRadius = 0f

    private var stickX = 0f
    private var stickY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
        baseRadius = min(w, h) / 2 * 0.9f
        stickRadius = baseRadius * 0.4f
        resetStick()
    }

    override fun onDraw(canvas: Canvas) {
        // Vẽ nền joystick
        canvas.drawCircle(centerX, centerY, baseRadius, bgPaint)
        // Vẽ stick hiện tại
        canvas.drawCircle(stickX, stickY, stickRadius, stickPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val dx = event.x - centerX
        val dy = event.y - centerY
        val distance = sqrt(dx * dx + dy * dy)
        val angle = atan2(dy, dx)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val cappedDist = min(distance, baseRadius)
                val normX = (cappedDist * cos(angle)) / baseRadius
                val normY = (cappedDist * sin(angle)) / baseRadius

                stickX = centerX + cappedDist * cos(angle)
                stickY = centerY + cappedDist * sin(angle)
                invalidate()

                listener?.onMove(normX, normY, angle, cappedDist / baseRadius)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                resetStick()
                invalidate()
                listener?.onRelease()
            }
        }

        return true
    }

    private fun resetStick() {
        stickX = centerX
        stickY = centerY
    }
}
