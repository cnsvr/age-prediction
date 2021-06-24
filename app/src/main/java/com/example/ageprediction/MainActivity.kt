package com.example.ageprediction

import android.R.id.message
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.concurrent.thread


private const val FILE_NAME = "photo.jpg"
private lateinit var photoFile: File

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btnTakePicture : Button
    private lateinit var imageView: ImageView

    var address:String = "192.168.1.33"
    var rotatedBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTakePicture = findViewById(R.id.btnTakePicture)
        imageView = findViewById(R.id.imageView)
        btnTakePicture.setOnClickListener(this)

    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onClick(view: View?) {
        //startAnalyze()

        when(view?.id){
            R.id.btnTakePicture->{
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoFile = getPhotoFile(FILE_NAME)

                val fileProvider = FileProvider.getUriForFile(this, "com.example.file", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

                if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                    startForResult.launch(takePictureIntent)
                } else {
                    Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            val ei = ExifInterface(photoFile.absolutePath)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

            // Rotation

            rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(takenImage, 90F)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(takenImage, 180F)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(takenImage, 270F)
                ExifInterface.ORIENTATION_NORMAL -> takenImage
                else -> takenImage
            }

            imageView.setImageBitmap(rotatedBitmap)
             try {

                //Convert bitmap to byte array
                val bos = ByteArrayOutputStream()
                 rotatedBitmap!!.compress(Bitmap.CompressFormat.PNG, 90, bos) // YOU can also save it in JPEG
                val bitmapdata = bos.toByteArray()


            } catch (e: Exception) {
                e.printStackTrace()
            }
            val analysebutton = findViewById(R.id.analysebutton) as Button
            analysebutton.setVisibility(View.VISIBLE);
            analysebutton.setOnClickListener {
                Toast.makeText(this@MainActivity, "Analyzing is started.", Toast.LENGTH_SHORT).show()
                startAnalyze()
            }
        }
    }

    fun startAnalyze(){
        runOnUiThread {
            var age : String = ""
            val file = FileDataPart.from(photoFile.absolutePath , name = "image")
            Fuel.upload("http://3.128.184.236:5000/send_image")
                .add(file)
                .response { _, response, result ->
                    println("response.body "+response.body())
                    val (bytes, error) = result
                    if (bytes != null) {
                        println("[response bytes] ${String(bytes)}")
                        age = String(bytes)
                    }
                }.join()

            var dialog: Dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.activity_result)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val boyImage: ImageView = dialog.findViewById<ImageView>(R.id.boyImage)

            val girlImage: ImageView = dialog.findViewById<ImageView>(R.id.girlImage)
            val ageText: TextView = dialog.findViewById<TextView>(R.id.ageText)

            if (age == "1-2"){
                boyImage.setImageResource(R.drawable.firstboy);
                girlImage.setImageResource(R.drawable.firstgirl);
                ageText.text = "Your age grup is : 1-2 "
            }else if (age ==  "3-9"){
                boyImage.setImageResource(R.drawable.secondboy);
                girlImage.setImageResource(R.drawable.secondgirl);
                ageText.text = "Your age grup is : 3-9 "
            }
            else if (age == "10-20"){
                boyImage.setImageResource(R.drawable.thirdboy);
                girlImage.setImageResource(R.drawable.thirdgirl);
                ageText.text = "Your age grup is : 10-20 "
            }
            else if (age == "21-27"){
                boyImage.setImageResource(R.drawable.forthboy);
                girlImage.setImageResource(R.drawable.forthgirl);
                ageText.text = "Your age grup is : 21-27 "
            }
            else if (age == "28-45"){
                boyImage.setImageResource(R.drawable.fifthboy);
                girlImage.setImageResource(R.drawable.fifthgirl);
                ageText.text = "Your age grup is : 28-45 "
            }
            else if (age == "46-65"){
                boyImage.setImageResource(R.drawable.sixthboy);
                girlImage.setImageResource(R.drawable.sixthgirl);
                ageText.text = "Your age grup is : 46-65 "
            }else{
                boyImage.setImageResource(R.drawable.lastboy);
                girlImage.setImageResource(R.drawable.lastwomen);
                ageText.text = "Your age grup is : +65 "
            }

            val btnOk:Button = dialog.findViewById<Button>(R.id.btnOK)

            btnOk.setOnClickListener(View.OnClickListener () {
                dialog.dismiss()
            })
            dialog.show()
        }

    }



    private fun getPhotoFile(fileName: String): File {
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

}