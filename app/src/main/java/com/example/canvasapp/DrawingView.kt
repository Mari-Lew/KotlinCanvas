package com.example.canvasapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * View that will be visible in the main activity. This is what we will be drawing
 */
class DrawingView(context: Context, attribs: AttributeSet): View(context, attribs)
{
    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null //holds style / color / etc
    private var mCanvasPaint: Paint? = null
    private var canvas: Canvas? = null

    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK

    init {
        startDrawingApp()
    }

    private fun startDrawingApp()
    {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)

        mDrawPaint!!.color = color //we know that mDrawPaint isn't empty!
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND

        //Set paint
        mCanvasPaint = Paint(Paint.DITHER_FLAG)

        //Set brush size
        mBrushSize = 10.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(drawCanvas: Canvas) {
        super.onDraw(drawCanvas)
        drawCanvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        if(!mDrawPath!!.isEmpty)
        {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            drawCanvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action) //same 3 things when using a motion event
        {
            //Finger on screen
                MotionEvent.ACTION_DOWN -> {
                    mDrawPath!!.color = color
                    mDrawPath!!.brushThickness = mBrushSize

                    mDrawPath!!.reset() //clear any lines
                    mDrawPath!!.moveTo(touchX!!,touchY!!)
                }
            //Dragging it to draw
                MotionEvent.ACTION_MOVE ->
                {
                    mDrawPath!!.lineTo(touchX!!,touchY!!)
                }
            //Release the touch
                MotionEvent.ACTION_UP ->
                {
                    mDrawPath = CustomPath(color, mBrushSize)
                }
            else -> return false
        }
        invalidate() //invalidates the whole view
        return true

    }

    /**
     * Should only be allowed to be used by this class
     */
    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float ) : Path() {

    }

}