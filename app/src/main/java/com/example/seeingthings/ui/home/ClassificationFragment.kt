package com.example.seeingthings.ui.home

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.SyncStateContract
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.seeingthings.BuildConfig
import com.example.seeingthings.R
import com.example.seeingthings.camera.AutoFitTextureView
import com.example.seeingthings.tflite.Classifier_
import com.example.seeingthings.tflite.ImageClassifier
import com.example.seeingthings.tflite.RealTimeClassifier
import com.example.seeingthings.utils.*
import com.example.seeingthings.utils.ImageUtils.scaleBitmap
import kotlinx.android.synthetic.main.custom_layout.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ClassificationFragment : Fragment() {

    private lateinit var classificationViewModel: ClassificationViewModel
    private val mInputSize = 224
    private val mModelPath = "mobilenet_v1_1.0_224_quant.tflite"
    private val mLabelPath = "labels_mobilenet_quant_v1_224.txt"
    private lateinit var classifier: RealTimeClassifier

    private lateinit var textureView: AutoFitTextureView
    private lateinit var itemNameTextView: TextView
    private lateinit var confidenceTextView: TextView
    private lateinit var titleTextView: TextView

    private lateinit var mPhotoFile: File
    private lateinit var mPhotoUri: Uri
    private lateinit var profilePics: ImageView
    private lateinit var currentPhotoPath: String
    private lateinit var bitmap: Bitmap
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        classificationViewModel =
                ViewModelProviders.of(this).get(ClassificationViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        classificationViewModel.text.observe(viewLifecycleOwner, Observer {
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        itemNameTextView = view.findViewById(R.id.tv_item)
        confidenceTextView = view.findViewById(R.id.tv_confidence)
        textureView = view.findViewById(R.id.texture)
        titleTextView = view.findViewById(R.id.program_title)
        titleTextView.setText(R.string.title_classify)
        confidenceTextView.visibility = View.GONE

        initClassifier()
        initViews()
    }

    private fun initClassifier() {
        classifier = RealTimeClassifier(requireActivity().assets, mModelPath, mLabelPath, mInputSize)
    }

    private fun initViews() {
        textureView.visibility = View.GONE
       dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takePictureIntent, GALLERY)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            bitmap = data!!.extras!!.get("data") as Bitmap
            image_view.visibility = View.VISIBLE
            image_view.setImageBitmap(bitmap)
            onClick()
        }
    }

    private fun onClick() {
       // bitmap = ((view as ImageView).drawable as BitmapDrawable).bitmap

        val result = classifier.recognizeImage(bitmap)

        run {
            itemNameTextView.text = result[0].title
          //  Toast.makeText(requireContext(), result[0].title, Toast.LENGTH_SHORT).show()
        }
    }

}