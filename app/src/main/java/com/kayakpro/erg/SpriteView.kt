package com.kayakpro.erg

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import android.util.Log

@SuppressLint("ViewConstructor")
class SpriteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var spriteBitmap: Bitmap? = null
    private var frame = 0
    private var frameCount = 11
    private var frameWidth = 0
    private var frameHeight = 0
    private var frameDelay = 70L
    private var isSpriteAnimating = false

    private val handler = Handler(Looper.getMainLooper())

    private val spriteAnimator = object : Runnable {
        override fun run() {
            frame++
            if (frame >= frameCount) {
                frame = 0
            }
            invalidate()
            handler.postDelayed(this, frameDelay)
        }
    }

    fun setupSprite(assetFileName: String, delay: Long, fCount: Int) {
        spriteBitmap = BitmapFactory.decodeStream(context.assets.open(assetFileName))
        frameCount = fCount
        frameWidth = Math.round(spriteBitmap!!.width.toFloat() / frameCount)
        frameHeight = spriteBitmap!!.height
        frameDelay = delay
    }

    fun startSpriteAnimation() {
        if (spriteBitmap == null) return
        isSpriteAnimating = true
        frame = 0
        handler.post(spriteAnimator)
    }

    fun stopSpriteAnimation() {
        isSpriteAnimating = false
        handler.removeCallbacks(spriteAnimator)
    }

    fun isSpriteAnimating(): Boolean = isSpriteAnimating

    fun getCurrentFrameBitmap(): Bitmap? {
        val bmp = spriteBitmap ?: return null
        val fw = if (frameWidth > 0) frameWidth else Math.round(bmp.width.toFloat() / frameCount)
        val left = frame * fw
        val w = Math.min(fw, bmp.width - left)
        return Bitmap.createBitmap(bmp, left, 0, w, bmp.height)
    }

    override fun onDraw(canvas: Canvas) {
        if (isSpriteAnimating && spriteBitmap != null) {
            val bmp = spriteBitmap!!
            val fw = if (frameWidth > 0) frameWidth else Math.round(bmp.width.toFloat() / frameCount)
            val left = frame * fw
            val src = Rect(
                left,
                0,
                Math.min(left + fw, bmp.width),
                bmp.height
            )
            val dst = Rect(0, 0, width, height)
            canvas.drawBitmap(bmp, src, dst, null)
        } else {
            super.onDraw(canvas)
        }
    }
}
