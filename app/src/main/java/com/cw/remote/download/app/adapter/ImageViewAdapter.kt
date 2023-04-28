package com.cw.remote.download.app.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter

//class ImageViewAdapter {
//    companion object {
@BindingAdapter("bindingSrc")
//        @JvmStatic
fun bindingSrc(imageView: ImageView, resId: Int) {
    imageView.setImageResource(resId)
}
//    }
//}