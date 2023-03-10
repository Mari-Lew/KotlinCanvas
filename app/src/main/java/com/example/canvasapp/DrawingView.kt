package com.example.canvasapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
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

    //make the line persist on the screen
    private val mPaths = ArrayList<CustomPath>()
    private val undoPaths = ArrayList<CustomPath>()

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


        for(path in mPaths)
        {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            drawCanvas.drawPath(path, mDrawPaint!!)
        }

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
                    mPaths.add(mDrawPath!!)
                    mDrawPath = CustomPath(color, mBrushSize)
                }
            else -> return false
        }
        invalidate() //invalidates the whole view
        return true

    }

    fun undoAction()
    {
        if(mPaths.size > 0)
        {
            // receiving removed item and storing it in undoPaths
            undoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }
    fun setBrushSize(newSize: Float)
    {
        //want to take screen size into consideration
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
            )

        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String)
    {
        color = Color.parseColor(newColor)
    }

    /**
     * Should only be allowed to be used by this class
     */
    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float ) : Path() {

    }

}