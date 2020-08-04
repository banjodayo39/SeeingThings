package com.example.seeingthings.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.example.seeingthings.model.Recognition
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.Comparator
import kotlin.math.min

class RealTimeClassifier(
    assetManager: AssetManager,
    modelPath: String,
    labelPath: String,
    private val inputSize: Int
) {
    private lateinit var interpreter: Interpreter
    private lateinit var labelList: List<String>
    private val pixelSize: Int = 3
    private val maxResult = 3
    private val threshHold = 0.4f

    // set the interpreter option
    init {
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(5)
        tfliteOptions.setUseNNAPI(true)
        interpreter = Interpreter(loadModelFile(assetManager, modelPath), tfliteOptions)
        labelList = loadLabelList(assetManager, labelPath)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath) //get the file description of the model
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor) // open input stream
        //read the channel along with length and offset
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        // load the the model
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    //load the
    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val result = Array(1) { ByteArray(labelList.size) }
        interpreter.run(byteBuffer, result)
        return getSortedResult(result)
    }


    private fun addPixelValue(byteBuffer: ByteBuffer, intValue: Int): ByteBuffer {

        byteBuffer.put((intValue.shr(16) and 0xFF).toByte())
        byteBuffer.put((intValue.shr(8) and 0xFF).toByte())
        byteBuffer.put((intValue and 0xFF).toByte())
        return byteBuffer
    }

    /** Writes Image data into a `ByteBuffer`.  */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(inputSize * inputSize * pixelSize)
        imgData.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)

        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        SystemClock.uptimeMillis()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]
                addPixelValue(imgData, value)
            }
        }
        return imgData
    }

    // get the image with the highest probability
    private fun getSortedResult(labelProbArray: Array<ByteArray>): List<Recognition> {
        Log.d(
            "Classifier",
            "List Size:(%d, %d, %d)".format(
                labelProbArray.size,
                labelProbArray[0].size,
                labelList.size
            )
        )

        val pq = PriorityQueue(
            maxResult,
            Comparator<Recognition> { (_, _, confidence1), (_, _, confidence2)
                ->
                confidence1.compareTo(confidence2) * -1
            })

        for (i in labelList.indices) {
            val confidence = labelProbArray[0][i]
            if (confidence >= threshHold) {
                Log.d("confidence value:", "" + confidence)
                pq.add(
                    Recognition(
                        "" + i,
                        if (labelList.size > i) labelList[i] else "Unknown",
                        ((confidence).toFloat() / 255.0f))
                )
            }
        }
        Log.d("Classifier", "pqsize:(%d)".format(pq.size))

        val recognitions = ArrayList<Recognition>()
        val recognitionsSize = min(pq.size, maxResult)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        return recognitions
    }
}