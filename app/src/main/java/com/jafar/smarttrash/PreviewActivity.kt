package com.jafar.smarttrash

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.jafar.smarttrash.databinding.ActivityPreviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var yolov5TFLiteDetector: Yolov5TFLiteDetector
    private lateinit var bitmap: Bitmap
    private lateinit var strokePaint: Paint
    private lateinit var boxPaint: Paint
    private lateinit var textPaint: Paint

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mRoot: DatabaseReference
    private lateinit var mRef: DatabaseReference
    private lateinit var userId: String

    private var jumlahDeteksi: Int = 0
    private var currentPointUser: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = mAuth.currentUser?.uid.toString()
        mRoot = FirebaseDatabase.getInstance().reference
        mRef = mRoot.child("users").child(userId)

        mRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                currentPointUser = user?.score!!
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PreviewActivity, "Gagal membaca data dari Firebase", Toast.LENGTH_SHORT).show()
            }
        })

        val uriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val imageUri = Uri.parse(uriString)

        if (imageUri != null) {
            bitmap = loadBitmapWithOrientation(imageUri)

            Glide.with(this)
                .load(imageUri)
                .into(binding.ivPreview)
        } else {
            Glide.with(this)
                .load(R.mipmap.ic_launcher)
                .into(binding.ivPreview)
            Log.e(TAG, "Error")
        }

        yolov5TFLiteDetector = Yolov5TFLiteDetector()
        yolov5TFLiteDetector.modelFile = "best-fp16.tflite"
        yolov5TFLiteDetector.initialModel(this)

        setUpBoundingBox()

        binding.btnPredict.setOnClickListener {
            predict()
        }

        binding.btnSelesai.setOnClickListener {
            currentPointUser += jumlahDeteksi
            mRef.child("score").setValue(currentPointUser)
            val intentToHome = Intent(this, HomeActivity::class.java)
            intentToHome.putExtra(EXTRA_SAMPAH, jumlahDeteksi.toString())
            startActivity(intentToHome)
            finish()
        }
    }

    private fun predict() {
        binding.scanAnimation.visibility = View.VISIBLE

        val startTime = System.currentTimeMillis()

        // Use Kotlin coroutine to run object detection in a background thread
        lifecycleScope.launch(Dispatchers.Default) {
            val recognitions = yolov5TFLiteDetector.detect(bitmap)

            // Record the end time
            val endTime = System.currentTimeMillis()

            // Calculate the elapsed time
            val elapsedTime = endTime - startTime

            // Update UI on the main thread with the detection results
            withContext(Dispatchers.Main) {
                processDetectionResults(recognitions)
                binding.scanAnimation.visibility = View.GONE
                binding.btnPredict.visibility = View.GONE
                var teksInformasiDeteksi: String
                if (jumlahDeteksi == 0) {
                    binding.btnSelesai.visibility = View.GONE // Memastikan tombol btn selesai tidak muncul
                    binding.llBerhasilDeteksi.visibility = View.VISIBLE
                    teksInformasiDeteksi = "Yah tidak ada yang terdeteksi. Silahkan coba lagi"
                } else {
                    binding.btnSelesai.visibility = View.GONE // Memastikan tombol btn selesai tidak muncul
                    binding.llBerhasilDeteksi.visibility = View.VISIBLE
                    binding.btnSelesai.visibility = View.VISIBLE
                    teksInformasiDeteksi = "Berhasil deteksi $jumlahDeteksi sampah dalam $elapsedTime ms"
                }
                binding.tvInformasiDeteksi.text = teksInformasiDeteksi
            }
        }
    }

    private fun processDetectionResults(recognitions: List<Recognition>) {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        for (recognition in recognitions) {
            if (recognition.confidence > 0.6) {
                val location = recognition.location
                chooseBoundingBox(recognition.labelId)
                canvas.drawRect(location, boxPaint)
                canvas.drawRect(location, strokePaint)
                canvas.drawText("${recognition.labelName}:${String.format("%.2f", recognition.confidence)}", location.left, location.top, textPaint)
                jumlahDeteksi++
            }
        }

        Glide.with(this)
            .asBitmap()
            .load(mutableBitmap)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(binding.ivPreview)
    }

    private fun loadBitmapWithOrientation(imageUri: Uri): Bitmap {
        // Load the bitmap from the URI
        val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        // Get the image orientation from Exif data
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        var orientation = ExifInterface.ORIENTATION_UNDEFINED

        if (inputStream != null) {
            try {
                val exifInterface = ExifInterface(inputStream)
                orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                inputStream.close()
            }
        }

        // Rotate the bitmap based on the orientation
        return rotateBitmap(originalBitmap, orientation)
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun setUpBoundingBox() {
        strokePaint = Paint()
        strokePaint.strokeWidth = 10F
        strokePaint.style = Paint.Style.STROKE

        boxPaint = Paint()
        boxPaint.style = Paint.Style.FILL

        textPaint = Paint()
        textPaint.textSize = 200F
        textPaint.style = Paint.Style.FILL
    }

    private fun chooseBoundingBox(label: Int) {
        when (label) {
            // Jika Anorganik
            0 -> {
                strokePaint.color = Color.BLUE
                boxPaint.color = Color.argb(50, 0, 0, 255)
                textPaint.color = Color.BLUE
            }
            // Jika Organik
            1 -> {
                strokePaint.color = Color.GREEN
                boxPaint.color = Color.argb(50, 0, 255, 0)
                textPaint.color = Color.GREEN
            }
            // Jika B3
            2 -> {
                strokePaint.color = Color.RED
                boxPaint.color = Color.argb(50, 255, 0, 0)
                textPaint.color = Color.RED
            }
        }
    }

    companion object {
        private val TAG = PreviewActivity::class.java.simpleName
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_SAMPAH = "extra_sampah"
    }
}