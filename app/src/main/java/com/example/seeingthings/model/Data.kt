package com.example.seeingthings.model

import android.graphics.RectF

data class Recognition(
    var id: String = "",
    var title: String = "",
    var confidence: Float = 0F
) {
    override fun toString(): String {
        return "Title = $title, Confidence = $confidence)"
    }
}