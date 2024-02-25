package com.pandacat.simplechaserun.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.pandacat.simplechaserun.R

object BitmapUtil {
    fun convertToBitmap(drawable: VectorDrawableCompat): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun getBitmapDescFromVector(context: Context, vectorId: Int) : BitmapDescriptor?
    {
        val vector = VectorDrawableCompat.create(context.resources, vectorId, context.theme)
        vector?.let {
            return BitmapDescriptorFactory.fromBitmap(convertToBitmap(it)) }
        return null
    }


}