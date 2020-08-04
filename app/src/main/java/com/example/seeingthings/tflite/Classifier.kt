package com.example.seeingthings.tflite

import android.graphics.Bitmap
import android.graphics.RectF

/** Generic interface for interacting with different recognition engines.  */
interface Classifier {

    val statString: String
    fun recognizeImage(bitmap: Bitmap): List<Recognition>

    fun enableStatLogging(debug: Boolean)

    fun close()

    fun setNumThreads(numThreads: Int)

    fun setUseNNAPI(isChecked: Boolean)

    /** An immutable result returned by a Classifier describing what was recognized.  */
    class Recognition(
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private val id: String?,
        /** Display name for the recognition.  */
        val title: String?,
        /**
         * A sortable score for how good the recognition is relative to others. Higher should be better.
         */
        val confidence: Float,
        /** Optional location within the source image for the location of the recognized object.  */
        internal var location: RectF
    ) {


        override fun toString(): String {
            var resultString = ""
            if (id != null) {
                resultString += "[$id] "
            }

            if (title != null) {
                resultString += "$title "
            }

            resultString += String.format("(%.1f%%) ", confidence * 100.0f)

            resultString += "$location "

            return resultString.trim { it <= ' ' }
        }
    }
}
