package com.example.canvasapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var drawView: DrawingView? = null
    private var ib_brush: ImageButton? = null;
    private var ib_undo: ImageButton? = null;
    private var mimageButtonCurPaint: ImageButton? = null;
    var customProgressDialog: Dialog? = null

    val openGalleryLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        result ->
        if(result.resultCode == RESULT_OK && result.data != null)
        {
            val imageBG: ImageView = findViewById(R.id.backgroundImage)

            //gives us the actual image
            imageBG.setImageURI(result.data?.data)
        }
    }

    private val requestPermissions : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {
            permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value

                if(isGranted)
                {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission granted; able to access storage files.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val pickIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                    openGalleryLauncher.launch(pickIntent)

                }
                else
                {
                    if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE)
                    {
                        Toast.makeText(
                            this@MainActivity,
                            "Permission denied; unable to access storage files.",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
        }

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

        ib_undo = findViewById(R.id.undoId);
        ib_undo?.setOnClickListener{
            drawView?.undoAction()
        }

        val ibGallery : ImageButton = findViewById(R.id.galleryId)
        ibGallery.setOnClickListener {
        requestStoragePermission()
        }
        
        val ib_save: ImageButton = findViewById(R.id.saveButton)
        ib_save.setOnClickListener{
            if(isReadStorageAllowed())
            {
                showProgressDialog()
                lifecycleScope.launch{
                    val flDrawView : FrameLayout = findViewById(R.id.drawingViewContainer)
                    saveBitmapFile(getBitmapFromView(flDrawView))

                }
            }
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

    private fun isReadStorageAllowed() : Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }


    private fun requestStoragePermission()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        )
        {
            showRationaleDialog("Canvas App", "Canvas App" + "needs to access your External Storage.")
        } else
        {
            requestPermissions.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }
    
    private fun getBitmapFromView(view: View) : Bitmap {
    val returnBitmap = Bitmap.createBitmap(view.width,
        view.height,
        Bitmap.Config.ARGB_8888)
    val canvas = Canvas(returnBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null)
        {
            bgDrawable.draw(canvas)
        }
        else
        {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnBitmap

    }



    private fun showRationaleDialog(
        title: String,
        message: String,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    //Coroutines
    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String {
        var result = "" //where we store image
        withContext(Dispatchers.IO)
        {
            if(mBitmap != null)
            {
                try{
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val file = File(externalCacheDir?.absoluteFile.toString() + File.separator + "CanvasApp_" + System.currentTimeMillis() / 1000 + ".png")
                    val fileOutput = FileOutputStream(file)
                    fileOutput.write(bytes.toByteArray())
                    fileOutput.close()

                    result = file.absolutePath //where the file is

                    //Let user know where the file is stored
                    runOnUiThread{
                        cancelProgressDialog()
                        if(result.isNotEmpty())
                        {
                            Toast.makeText(
                                this@MainActivity,
                                "File saved successfully: $result ",
                                Toast.LENGTH_SHORT
                            ).show()

                            shareImage(result)
                        }
                    }
                } catch (e:Exception)
                {
                    result = ""
                    e.printStackTrace()
                }
            }
        }

        return result
    }

    private fun shareImage(result:String){

         MediaScannerConnection.scanFile(
            this@MainActivity, arrayOf(result), null
        ) { path, uri ->
            // share image after it's stored in the storage.
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
             //Extra details so it knows which pile it should use
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                uri
            )
             shareIntent.type =
                "image/png"
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share"
                )
            )
        }
    }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

        //Start the dialog and display it on screen.
        customProgressDialog?.show()
    }

    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    private fun cancelProgressDialog() {
        // does it exist?
        if (customProgressDialog != null) {
            //if it is, dismiss it
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }
}