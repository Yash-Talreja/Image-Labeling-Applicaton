package com.example.imagelabelapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var objectImage: ImageView
    private lateinit var imageLabel: TextView
    private lateinit var captureImagebtn: Button
    private lateinit var imagelabler:ImageLabeler
    private lateinit var cameralauncher: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        objectImage=findViewById(R.id.objectImage)
        imageLabel=findViewById(R.id.imageLabel)
        captureImagebtn=findViewById(R.id.CaptureImagebtn)

        checkcamerapermission()

        imagelabler=ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        cameralauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == Activity.RESULT_OK){
                val extras=result.data?.extras
                val imageBitmap=extras?.getParcelable("data",Bitmap::class.java)

                if (imageBitmap!=null)
                {
                    objectImage.setImageBitmap(imageBitmap)
                    labelImage(imageBitmap)

                }else
                {
                    imageLabel.text="Unable to Capture Image"

                }
            }
        }

        captureImagebtn.setOnClickListener {
            val captureimage=Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)

            if (captureimage.resolveActivity(packageManager)!=null)
            {
                cameralauncher.launch(captureimage)

            }
        }

    }

    private fun labelImage(bitmap: Bitmap)
    {

        val inputimage=InputImage.fromBitmap(bitmap,0)

        imagelabler.process(inputimage).addOnSuccessListener { labellist ->

            displaylabel(labellist)

        }.addOnFailureListener {e ->

            imageLabel.text="Error: ${e.message}"

        }

    }

    private fun displaylabel(labels:List<ImageLabel>)
    {

        if (labels.isNotEmpty())
        {
            val mostconfidentlabel=labels[0]
            imageLabel.text="${mostconfidentlabel.text}"
        }else
        {
            imageLabel.text="No Label Detected"
        }
    }

    private fun checkcamerapermission()
    {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),1)

        }

        }

}