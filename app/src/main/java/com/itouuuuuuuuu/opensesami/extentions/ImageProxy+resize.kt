package com.itouuuuuuuuu.opensesami.extentions

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

fun Bitmap.resize(magnification: Double): Bitmap {
    val width: Int = (this.width * magnification).toInt()
    val height: Int = (this.height * magnification).toInt()
    return Bitmap.createScaledBitmap(this, width, height, false)
}