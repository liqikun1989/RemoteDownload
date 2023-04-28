package com.cw.remote.download.app.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.nio.charset.StandardCharsets

object QrCodeUtil {
    fun createQRImage(content: String?, widthPix: Int, heightPix: Int): Bitmap? {
        try {
            // 判断URL合法性
            if (content == null || "" == content || content.isEmpty()) {
                return null
            }
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8.name()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.MARGIN] = 1
            var bitMatrix: BitMatrix? = null
            try {
                bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints)
            } catch (e: WriterException) {
                return null
            }
            val pixels = IntArray(widthPix * heightPix)
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (y in 0 until heightPix) {
                for (x in 0 until widthPix) {
                    if (bitMatrix != null) {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * widthPix + x] = -0x1000000
                        } else {
                            pixels[y * widthPix + x] = -0x1
                        }
                    } else {
                        return null
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            val bitmap: Bitmap? = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888)
            bitmap!!.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}