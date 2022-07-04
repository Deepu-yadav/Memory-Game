package com.example.memorygame.utils

import android.graphics.Bitmap


object BitmapScalar {

    // Scale and maintain aspect ratio given a desired width
    // BitmapScaler.scaleToFitWidth(bitmap,100)
    fun scaleToFitWidth(b:Bitmap,width:Int):Bitmap{
        val factor=width /b.width.toFloat()
        return Bitmap.createScaledBitmap(b,width,(b.height * factor).toInt(),true)
    }
    fun scaleToFitHeight(b:Bitmap,height:Int):Bitmap{
        val factor=height /b.width.toFloat()
        return Bitmap.createScaledBitmap(b,(b.width * factor).toInt(),height,true)
    }


}
