package com.scheellarsen

import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException


class ShowCamera(context: Context?,camera:Camera?):SurfaceView(context),SurfaceHolder.Callback{

    var camera:Camera?=null
    var HOLDER:SurfaceHolder?=null
    var mSupportedPreviewSizes: List<Camera.Size>? = null
    var mPreviewSize: Camera.Size? = null
    init {
        super.getContext()
        this.camera = camera
        mSupportedPreviewSizes = camera!!.getParameters().getSupportedPreviewSizes();
        HOLDER = holder
        HOLDER!!.addCallback(this)
        HOLDER!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {


        if(camera!=null) {

            try {
                var params: Camera.Parameters = camera!!.parameters
                var sizes: List<Camera.Size> = params.supportedPictureSizes
                var mSize: Camera.Size? = null
                var a=1
                for (size: Camera.Size in sizes) {
                    if(a==2) {
                        mSize = size
                        break;
                    }
                    a++
                }

                if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    params.set("orientation", "portrait")
                    camera!!.setDisplayOrientation(90)
                    params.set("rotation", 90)
                    params.setRotation(90)
                } else {
                    params.set("orientation", "landscape")
                    camera!!.setDisplayOrientation(0)
                    params.set("rotation", 0)
                    params.setRotation(0)
                }
                params.setPreviewSize(mPreviewSize!!.width, mPreviewSize!!.height);

                params.setPictureSize(mSize!!.width, mSize!!.height)


                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE

                camera!!.parameters = params
                camera!!.setPreviewDisplay(HOLDER)
                camera!!.startPreview();


            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        if(camera!=null) {
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
    }
    override fun onMeasure(widthMeasureSpec:Int, heightMeasureSpec:Int) {
        var width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec)
        var height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec)
        if (mSupportedPreviewSizes != null)
        {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height)
        }
        if (mPreviewSize != null)
        {
            var ratio:Float
            if (mPreviewSize!!.height >= mPreviewSize!!.width)
                ratio = mPreviewSize!!.height.toFloat() / mPreviewSize!!.width.toFloat()
            else
                ratio = mPreviewSize!!.width.toFloat()/ mPreviewSize!!.height.toFloat()
            setMeasuredDimension(width, (width * ratio).toInt())
        }
    }

private fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
    val ASPECT_TOLERANCE = 0.1
    val targetRatio = w.toDouble() / h
    if (sizes == null) return null

    var optimalSize: Camera.Size? = null
    var minDiff = java.lang.Double.MAX_VALUE

// Try to find an size match aspect ratio and size
    for (size in sizes) {
        val ratio = size.width.toDouble() / size.height
        if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
        if (Math.abs(size.height - h) < minDiff) {
            optimalSize = size
            minDiff = Math.abs(size.height - h).toDouble()
        }
    }

    // Cannot find the one match the aspect ratio, ignore the requirement
    if (optimalSize == null) {
        minDiff = java.lang.Double.MAX_VALUE
        for (size in sizes) {
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }
    }
    return optimalSize
}



    override fun surfaceCreated(holder: SurfaceHolder?) {



    }


}

