package com.cw.remote.download.app.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.cw.remote.download.app.R
import com.cw.remote.download.app.util.QrCodeUtil
import java.net.Inet4Address
import java.net.NetworkInterface


class QRdialogFragment : DialogFragment() {
    companion object {
        fun newInstance(path: String): QRdialogFragment {
            val qRdialogFragment = QRdialogFragment()
            val bundle = Bundle()
            bundle.putString("path", path)
            qRdialogFragment.arguments = bundle
            return qRdialogFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.apply {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = layoutParams
            setGravity(Gravity.CENTER)
        }
        return inflater.inflate(R.layout.dialog_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var ipv4Address: String? = null
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            if (networkInterface.name == "wlan0") {
                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        ipv4Address = inetAddress.getHostAddress()?.toString()
                        break
                    }
                }
            }
        }
        ipv4Address?.apply {
            QrCodeUtil.createQRImage("http://$this:8080/download?path=${arguments?.getString("path")}", 500, 500)?.apply {
                view.findViewById<ImageView>(R.id.ivQr).setImageBitmap(this)
            }
        }
    }
}

