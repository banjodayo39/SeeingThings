package com.example.seeingthings.ui.objectdetection

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.seeingthings.R
import com.example.seeingthings.ui.camera.DetectorActivity

class ObjectDetection : Fragment() {

    private lateinit var objectDetectionViewModel: ObjectDetectionViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        objectDetectionViewModel =
                ViewModelProviders.of(this).get(ObjectDetectionViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_detection, container, false)
        objectDetectionViewModel.text.observe(viewLifecycleOwner, Observer {
            val intent = Intent(requireActivity(), DetectorActivity::class.java)
            requireActivity().startActivity(intent)
        })
        return root
    }
}