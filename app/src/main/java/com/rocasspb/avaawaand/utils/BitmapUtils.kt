package com.rocasspb.avaawaand.utils

import android.graphics.Bitmap
import com.rocasspb.avaawaand.logic.RasterData

/**
 * Extension to convert shared RasterData to Android Bitmap.
 */
fun RasterData.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}
