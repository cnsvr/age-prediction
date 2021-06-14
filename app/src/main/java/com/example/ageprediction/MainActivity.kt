package com.example.ageprediction

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

private const val FILE_NAME = "photo.jpg"
private lateinit var photoFile: File

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btnTakePicture : Button
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTakePicture = findViewById(R.id.btnTakePicture)
        imageView = findViewById(R.id.imageView)
        btnTakePicture.setOnClickListener(this)

    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onClick(view: View?) {
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
        if (result.resultCode == Activity.RESULT_OK) {
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)

            imageView.setImageBitmap(takenImage)
        }
    }

    private fun getPhotoFile(fileName: String): File {
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }
}