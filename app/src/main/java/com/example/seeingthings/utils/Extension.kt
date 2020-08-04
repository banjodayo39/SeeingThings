package com.example.seeingthings.utils

import android.widget.Toast
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.showToast(text: String) {
    runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}
