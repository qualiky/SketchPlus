package com.example.sketchplus

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sketchplus.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    var brushSize: Float = 0f

    companion object {
        val REQUEST_EXT_STORAGE_PERMISSION_CODE = 110
        val PICK_PHOTO_CODE = 69
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var currentColor: Int = 0

        binding.drawingView.setBrushSize(1f)
        currentColor = binding.drawingView.color

        val brushSizer = findViewById<ImageButton>(R.id.ib_brush)


        brushSizer.setOnClickListener {
            //Toast.makeText(this,"Shown",Toast.LENGTH_SHORT).show()
            showBrushSizeSelectorDialog()


        }

        binding.ibPalette.setOnClickListener {
            showColorSelectorDialog(binding.drawingView.color)
        }

        binding.ibBrush.setOnClickListener {
            binding.drawingView.onClickUndo()
        }


        binding.icGallery.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_PHOTO_CODE)

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_EXT_STORAGE_PERMISSION_CODE)
            }
        }

        binding.icSave.setOnClickListener {
            val bitmapSaved = Bitmap.createBitmap(binding.flowLayout.width, binding.flowLayout.height,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmapSaved)
            val bgDrawable = binding.ivBackground.background

            if(bgDrawable != null) {
                bgDrawable.draw(canvas)
            } else {
                canvas.drawARGB(255,255,255,255)
            }

            binding.flowLayout.draw(canvas)

            BitmapAsyncTask(bitmapSaved).execute()
        }
    }

    private inner class BitmapAsyncTask(val mBitmap: Bitmap) : AsyncTask<Any,Void,String>() {

        override fun doInBackground(vararg params: Any?): String {
            var result =""

            if(mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f = File(externalCacheDir!!.absoluteFile.toString() + File.separator + "SketchPlus_" + System.currentTimeMillis()/1000 + ".png")
                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if(!result.isNullOrEmpty()) {
                Toast.makeText(applicationContext,"Image saved successfully!",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext,"Image could not be saved!",Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == PICK_PHOTO_CODE) {
            try {
                if(data!!.data != null) {
                    binding.ivBackground.visibility = View.VISIBLE
                    binding.ivBackground.setImageURI(data.data)
                } else {
                    Snackbar.make(binding.root,"Cannot parse image!",Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == REQUEST_EXT_STORAGE_PERMISSION_CODE) {
            Toast.makeText(this,"Permission accessed!",Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this,"Permission denied!",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showColorSelectorDialog(currentC: Int) {


        var currentColor = currentC
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_color_picker)
        dialog.setTitle("Color Selection")

        val seekBarRed = dialog.findViewById<SeekBar>(R.id.seekbarRed)
        val seekBarGreen = dialog.findViewById<SeekBar>(R.id.seekbarGreen)
        val seekBarBlue = dialog.findViewById<SeekBar>(R.id.seekbarBlue)
        val seekBarAlpha = dialog.findViewById<SeekBar>(R.id.seekbarAlpha)

        val imgDetail = dialog.findViewById<ImageView>(R.id.colorPreview)
        imgDetail.setBackgroundColor(currentColor)

        var colorDrawable: ColorDrawable = imgDetail.background as ColorDrawable

        var alpha = colorDrawable.alpha

        var red = Color.red(currentColor)
        var green = Color.green(currentColor)
        var blue = Color.blue(currentColor)

        seekBarRed.setProgress(red,true)
        seekBarGreen.setProgress(green,true)
        seekBarBlue.setProgress(blue,true)
        seekBarAlpha.setProgress(alpha,true)

        var seekbarValueRed = 0
        var seekbarValueGreen = 0
        var seekbarValueBlue = 0
        var seekbarValueAlpha = 0

        var textRed = dialog.findViewById<TextView>(R.id.tvRed)
        var textBlue = dialog.findViewById<TextView>(R.id.tvBlue)
        var textGreen = dialog.findViewById<TextView>(R.id.tvGreen)
        var textAlpha = dialog.findViewById<TextView>(R.id.tvAlpha)

        textRed.text = "R: $red"
        textBlue.text = "B: $blue"
        textGreen.text = "G: $green"
        textAlpha.text = "A: $alpha"

        val btnColorSkin = dialog.findViewById<ImageView>(R.id.colorSkinButton)
        val btnColorBlack = dialog.findViewById<ImageView>(R.id.colorPureBlackButton)
        val btnColorRed = dialog.findViewById<ImageView>(R.id.colorRedButton)
        val btnColorGreen = dialog.findViewById<ImageView>(R.id.colorGreenButton)
        val btnColorBlue = dialog.findViewById<ImageView>(R.id.colorBlueButton)
        val btnColorYellow = dialog.findViewById<ImageView>(R.id.colorYellowButton)
        val btnColorLollipop = dialog.findViewById<ImageView>(R.id.colorLollipopButton)
        val btnColorMagentish = dialog.findViewById<ImageView>(R.id.colorMagentishButton)

        btnColorSkin.setOnClickListener {
            currentColor = Color.parseColor("#ffffcc99")
            binding.drawingView.color = currentColor
            imgDetail.setBackgroundColor(currentColor)
            colorDrawable = imgDetail.background as ColorDrawable
            alpha = colorDrawable.alpha
            red = Color.red(currentColor)
            green = Color.green(currentColor)
            blue = Color.blue(currentColor)
            textRed.text = "R: $red"
            textBlue.text = "B: $blue"
            textGreen.text = "G: $green"
            textAlpha.text = "A: $alpha"
            seekBarRed.setProgress(red,true)
            seekBarGreen.setProgress(green,true)
            seekBarBlue.setProgress(blue,true)
            seekBarAlpha.setProgress(alpha,true)
        }

        btnColorBlack.setOnClickListener {
            currentColor = Color.parseColor("#ff000000")
            binding.drawingView.color = currentColor
            imgDetail.setBackgroundColor(currentColor)
            colorDrawable = imgDetail.background as ColorDrawable
            alpha = colorDrawable.alpha
            red = Color.red(currentColor)
            green = Color.green(currentColor)
            blue = Color.blue(currentColor)
            textRed.text = "R: $red"
            textBlue.text = "B: $blue"
            textGreen.text = "G: $green"
            textAlpha.text = "A: $alpha"
            seekBarRed.setProgress(red,true)
            seekBarGreen.setProgress(green,true)
            seekBarBlue.setProgress(blue,true)
            seekBarAlpha.setProgress(alpha,true)
        }

        btnColorRed.setOnClickListener {
            currentColor = Color.parseColor("#ffff0000")
            binding.drawingView.color = currentColor
            imgDetail.setBackgroundColor(currentColor)
            colorDrawable = imgDetail.background as ColorDrawable
            alpha = colorDrawable.alpha
            red = Color.red(currentColor)
            green = Color.green(currentColor)
            blue = Color.blue(currentColor)
            textRed.text = "R: $red"
            textBlue.text = "B: $blue"
            textGreen.text = "G: $green"
            textAlpha.text = "A: $alpha"
            seekBarRed.setProgress(red,true)
            seekBarGreen.setProgress(green,true)
            seekBarBlue.setProgress(blue,true)
            seekBarAlpha.setProgress(alpha,true)
        }

        btnColorGreen.setOnClickListener {
            currentColor = Color.parseColor("#ff00ff00")
            binding.drawingView.color = currentColor
            imgDetail.setBackgroundColor(currentColor)
            colorDrawable = imgDetail.background as ColorDrawable
            alpha = colorDrawable.alpha
            red = Color.red(currentColor)
            green = Color.green(currentColor)
            blue = Color.blue(currentColor)
            textRed.text = "R: $red"
            textBlue.text = "B: $blue"
            textGreen.text = "G: $green"
            textAlpha.text = "A: $alpha"
            seekBarRed.setProgress(red,true)
            seekBarGreen.setProgress(green,true)
            seekBarBlue.setProgress(blue,true)
            seekBarAlpha.setProgress(alpha,true)
        }

        btnColorBlue.setOnClickListener {
            currentColor = Color.parseColor("#ff0000ff")
            binding.drawingView.color = currentColor
            imgDetail.setBackgroundColor(currentColor)
            colorDrawable = imgDetail.background as ColorDrawable
            alpha = colorDrawable.alpha
            red = Color.red(currentColor)
            green = Color.green(currentColor)
            blue = Color.blue(currentColor)
            textRed.text = "R: $red"
            textBlue.text = "B: $blue"
            textGreen.text = "G: $green"
            textAlpha.text = "A: $alpha"
            seekBarRed.setProgress(red,true)
            seekBarGreen.setProgress(green,true)
            seekBarBlue.setProgress(blue,true)
            seekBarAlpha.setProgress(alpha,true)
        }

        btnColorYellow.setOnClickListener {
            currentColor = Color.parseColor("#ffffff00")
            binding.drawingView.color = currentColor
            imgDetail.setBackgroundColor(currentColor)
            colorDrawable = imgDetail.background as ColorDrawable
            alpha = colorDrawable.alpha
            red = Color.red(currentColor)
            green = Color.green(currentColor)
            blue = Color.blue(currentColor)
            textRed.text = "R: $red"
            textBlue.text = "B: $blue"
            textGreen.text = "G: $green"
            textAlpha.text = "A: $alpha"
            seekBarRed.setProgress(red,true)
            seekBarGreen.setProgress(green,true)
            seekBarBlue.setProgress(blue,true)
            seekBarAlpha.setProgress(alpha,true)
        }

        btnColorLollipop.setOnClickListener {
            currentColor = Color.parseColor("#ff35a79c")
            binding.drawingView.color = currentColor
            imgDetail.setBackgroundColor(currentColor)
            colorDrawable = imgDetail.background as ColorDrawable
            alpha = colorDrawable.alpha
            red = Color.red(currentColor)
            green = Color.green(currentColor)
            blue = Color.blue(currentColor)
            textRed.text = "R: $red"
            textBlue.text = "B: $blue"
            textGreen.text = "G: $green"
            textAlpha.text = "A: $alpha"
            seekBarRed.setProgress(red,true)
            seekBarGreen.setProgress(green,true)
            seekBarBlue.setProgress(blue,true)
            seekBarAlpha.setProgress(alpha,true)
        }

        btnColorMagentish.setOnClickListener {
            currentColor = Color.parseColor("#ffa349b1")
            binding.drawingView.color = currentColor
            imgDetail.setBackgroundColor(currentColor)
            colorDrawable = imgDetail.background as ColorDrawable
            alpha = colorDrawable.alpha
            red = Color.red(currentColor)
            green = Color.green(currentColor)
            blue = Color.blue(currentColor)
            textRed.text = "R: $red"
            textBlue.text = "B: $blue"
            textGreen.text = "G: $green"
            textAlpha.text = "A: $alpha"
            seekBarRed.setProgress(red,true)
            seekBarGreen.setProgress(green,true)
            seekBarBlue.setProgress(blue,true)
            seekBarAlpha.setProgress(alpha,true)
        }

        seekBarRed.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    seekBarRed.setProgress(progress,true)
                    red = seek.progress
                    currentColor = Color.argb(alpha,red,green,blue)
                    imgDetail.setBackgroundColor(currentColor)
                    binding.drawingView.color = currentColor
                    textRed.text = "R: $red"
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {

            }

            override fun onStopTrackingTouch(seek: SeekBar) {

            }
        })

        seekBarGreen.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    seekBarGreen.setProgress(progress,true)
                    green = seek.progress
                    currentColor = Color.argb(alpha,red,green,blue)
                    imgDetail.setBackgroundColor(currentColor)
                    binding.drawingView.color = currentColor
                    textGreen.text = "G: $green"


                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {

            }

            override fun onStopTrackingTouch(seek: SeekBar) {

            }
        })

        seekBarBlue.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    seekBarBlue.setProgress(progress,true)
                    blue = seek.progress
                    currentColor = Color.argb(alpha,red,green,blue)
                    imgDetail.setBackgroundColor(currentColor)
                    binding.drawingView.color = currentColor
                    textBlue.text = "B: $blue"


                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {

            }

            override fun onStopTrackingTouch(seek: SeekBar) {

            }
        })

        seekBarAlpha.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    seekBarAlpha.setProgress(progress,true)
                    alpha = seek.progress
                    currentColor = Color.argb(alpha,red,green,blue)
                    imgDetail.setBackgroundColor(currentColor)
                    binding.drawingView.color = currentColor
                    textAlpha.text = "A: $alpha"

                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {

            }

            override fun onStopTrackingTouch(seek: SeekBar) {

            }
        })



        dialog.show()

    }

    private fun showBrushSizeSelectorDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_brush_size)
        dialog.setTitle("Brush Size")

        val seekbar = dialog.findViewById<SeekBar>(R.id.seekBar)
        val brushImg = dialog.findViewById<ImageView>(R.id.ibSize)
        val brushText = dialog.findViewById<TextView>(R.id.tvSize)

        brushImg.setBackgroundColor(binding.drawingView.color)

        seekbar.setProgress(brushSize.toInt(),true)

        val params = brushImg.layoutParams
        params.height = brushSize.toInt()*3
        brushImg.layoutParams = params

        brushText.text = "${brushSize.toInt()}"

        seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {

                    seekbar.setProgress(progress,true)

                    var params = brushImg.layoutParams
                    params.height = progress*3
                    brushImg.layoutParams = params

                    brushSize = progress.toFloat()
                    binding.drawingView.setBrushSize(brushSize)
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                //var width: Int = seekbar.width - seekbar.paddingLeft - seekbar.paddingRight
                //var thumbPos = seekbar.paddingLeft + width * seekbar.progress / seekbar.max
                seekbar.thumbOffset = seek.progress
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                //seekbar.setProgress(seek.progress,true)
                seekbar.thumbOffset = seek.progress
                brushText.text = seek.progress.toString()
            }
        })

        dialog.show()

    }

}