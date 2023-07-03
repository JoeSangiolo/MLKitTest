package com.example.mltest

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.example.mltest.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1001
        /*
        const val KEY_IMAGE_URI = "com.example.mltest.KEY_IMAGE_URI"
        const val KEY_IMAGE_MAX_WIDTH = "com.example.mltest.KEY_IMAGE_MAX_WIDTH"
        const val KEY_IMAGE_MAX_HEIGHT = "com.example.mltest.KEY_IMAGE_MAX_HEIGHT"
        const val KEY_SELECTED_SIZE = "com.example.mltest.KEY_SELECTED_SIZE"
        */
    }

    lateinit var binding: ActivityMainBinding
    lateinit var detector: ObjectDetector
    lateinit var labeler: ImageLabeler

    lateinit var recognizer: TextRecognizer

    var imageUri: Uri? = null

    /*
    var preview: ImageView? = null
    var imageUri: Uri? = null
    var imageMaxWidth = 0
    var imageMaxHeight = 0
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // defining ML Kit's Object Detection model
        val detectionOptions = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        detector = ObjectDetection.getClient(detectionOptions)

        // defining ML Kit's Image Labeling model
        val labelerOptions = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.85f)
            .build()
        labeler = ImageLabeling.getClient(labelerOptions)

        // instantiating MLKit's Text Recognition model
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        setContentView(binding.root)

        binding.btnOpenCamera.setOnClickListener {


            val intentPhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intentPhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

            resultLauncher.launch(intentPhoto)

            //ActivityResultContracts.StartActivityForResult()
            //startCameraIntentForResult()
        }
        /*
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI)
            imageMaxWidth = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH)
            imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT)
            //selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE)
        }

        val rootView = findViewById<View>(R.id.root)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    imageMaxWidth = rootView.width
                    imageMaxHeight = rootView.height

                    /*
                    if (SIZE_SCREEN == selectedSize) {
                        tryReloadAndDetectInImage()
                    }
                     */
                }
            })
        */
        binding.btnEval.setOnClickListener {
            //use ML Kit model to evaluate image
            if (imageBitmap != null) {
                var image = InputImage.fromBitmap(imageBitmap!!,0)
                /*
                detector.process(image)
                    .addOnSuccessListener { detectedObjects ->
                        //task completed successfully

                        for (detectedObject in detectedObjects) {
                            val boundingBox = detectedObject.boundingBox
                            val trackingId = detectedObject.trackingId
                            for (label in detectedObject.labels) {
                                val text = label.text
                                //text += label.text
                                if (PredefinedCategory.FOOD == text) {
                                    binding.tvGuess.text = "Food identified!"
                                }
                            }
                        }

                    }
                    .addOnFailureListener { e ->
                        // task failed with exception e
                    }
                */
                /*
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        // task completed successfully
                        if (labels.size > 0) {
                            val bestGuess = labels[0]
                            binding.tvGuess.text =
                                "That's a nice '${bestGuess.text}' you got there! " +
                                        "And I'm ${bestGuess.confidence * 100}% sure of it!"
                            binding.btnOpenCamera.text = "CAMERA"
                        } else {
                            binding.tvGuess.text = "I don't quite know what I'm looking at :/"
                            binding.btnOpenCamera.text = "Try again :p"
                        }
                    }
                    .addOnFailureListener { e ->
                        // task failed with exception e
                        binding.tvGuess.text = "ERROR: ${e.message}"
                    }
                 */
                val result = recognizer.process(image)
                    .addOnSuccessListener { recognizedText ->
                        //binding.tvGuess.text = recognizedText.text
                        binding.tvGuess.text = ""
                        for (block in recognizedText.textBlocks) {
                            val blockText = block.text
                            binding.tvGuess.append("${blockText.toString()} \n")
                        //    val blockCornerPoints = block.cornerPoints
                        //    val blockFrame = block.boundingBox
                        /*
                            for (line in block.lines) {
                                val lineText = line.text
                                binding.tvGuess.append("${lineText.toString()} \n")

                            //    val lineCornerPoints = line.cornerPoints
                            //    val lineFrame = line.boundingBox
                            //    for (element in line.elements) {
                            //        val elementText = element.text
                            //        val elementCornerPoints = element.cornerPoints
                            //        val elementFrame = element.boundingBox
                            //    }
                            }
                            */
                        }

                    }
                    .addOnFailureListener { e ->

                    }

            } else {
                //Error ${t.message}
            }
        }

        requestNeededPermission()
    }

    var imageBitmap: Bitmap? = null
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            imageBitmap = data!!.extras!!.get("data") as Bitmap
            imageUri = data!!.extras!!.get("imageUri") as Uri
            binding.ivPreview.setImageBitmap(imageBitmap)
            binding.ivPreview.visibility = View.VISIBLE
        }


    }


    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
                Toast.makeText(this,
                    "I need it for camera", Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION)
        } else {
            // we already have permission
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "CAMERA perm granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "CAMERA perm NOT granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /*
    private fun startCameraIntentForResult() { // Clean up last time's image
        imageUri = null
        preview!!.setImageBitmap(null)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {

            val values = ContentValues()

            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")

            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            /*
            startActivityForResult(
                takePictureIntent,
                REQUEST_CAMERA_PERMISSION
            )
            */
            resultLauncher.launch(takePictureIntent)
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
            KEY_IMAGE_URI,
            imageUri
        )
        outState.putInt(
            KEY_IMAGE_MAX_WIDTH,
            imageMaxWidth
        )
        outState.putInt(
            KEY_IMAGE_MAX_HEIGHT,
            imageMaxHeight
        )
        /*
        outState.putString(
            KEY_SELECTED_SIZE,
            selectedSize
        )
         */
    }
    */

    override fun onResume() {
        super.onResume()

        //val img: Image = athlete

        //val bmap = createBitmap(R.drawable.athlete)
        //R.drawable.athlete
        //val ipImg: InputImage = InputImage.fromMediaImage(R.drawable.athlete, 0)

        //detector = ObjectDetector()

    }



}