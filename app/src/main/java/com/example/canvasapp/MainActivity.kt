package com.example.canvasapp

import android.app.Dialog
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    private var drawView: DrawingView? = null
    private var ib_brush: ImageButton? = null;
    private var mimageButtonCurPaint: ImageButton? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawView = findViewById(R.id.drawingView)
        //drawView?.setBrushSize(20.toFloat())
        val paintColors = findViewById<LinearLayout>(R.id.colorBox);

        mimageButtonCurPaint = paintColors[1] as ImageButton;
        mimageButtonCurPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_pressed)
        )

        ib_brush = findViewById(R.id.brushId);
        ib_brush?.setOnClickListener{
            showBrushSizeChoiceDialog();
        }
    }

    private fun showBrushSizeChoiceDialog()
    {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.brush_size_dialog)
        brushDialog.setTitle("Brush Size : ")

        val smallButton : ImageButton = brushDialog.findViewById(R.id.smallBrush)
        smallButton.setOnClickListener(View.OnClickListener {
            drawView?.setBrushSize(5.toFloat());
            brushDialog.dismiss()
        })

        val mediumButton : ImageButton = brushDialog.findViewById(R.id.mediumBrush)
        mediumButton.setOnClickListener(View.OnClickListener {
            drawView?.setBrushSize(15.toFloat());
            brushDialog.dismiss()
        })

        val largeButton : ImageButton = brushDialog.findViewById(R.id.largeBrush)
        largeButton.setOnClickListener{
            drawView?.setBrushSize(25.toFloat());
            brushDialog.dismiss()
        }

        brushDialog.show();
    }

    fun paintClicked(view: View)
    {
       if(view !== mimageButtonCurPaint)
       {
           val imageButton = view as ImageButton
           val colorTag = imageButton.tag.toString()
           drawView?.setColor(colorTag)

           //selected should be set to pressed
           imageButton.setImageDrawable(
               ContextCompat.getDrawable(this, R.drawable.palette_pressed)
           )

           //unselected set to unpressed
           mimageButtonCurPaint?.setImageDrawable(
               ContextCompat.getDrawable(this, R.drawable.palette_normal)
           )

           mimageButtonCurPaint = view
       }
    }
}