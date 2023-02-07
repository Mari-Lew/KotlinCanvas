package com.example.canvasapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
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

    /**
     * Should only be allowed to be used by this class
     */
    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float ) : Path() {

    }

}