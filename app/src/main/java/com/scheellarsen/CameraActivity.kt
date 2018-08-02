package com.scheellarsen


import android.content.Context
import android.hardware.Camera
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity() {

    var camera: Camera? = null
    var showCamera:ShowCamera?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        val frameLayout = camera_frame

        val camera = Camera.open()

        showCamera = ShowCamera(this,camera)

        camera_frame.addView(showCamera)
    }
}
