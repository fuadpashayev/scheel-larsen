package com.scheellarsen

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.hardware.Camera
import android.util.Log
import android.view.Surface
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

                for (size: Camera.Size in sizes) {
                    mSize = size
                    break;
                }

                if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    params.set("orientation", "portrait")
                    camera!!.setDisplayOrientation(90)
                    params.set("rotation", 90)
                    params.setRotation(90)
                }
                params.setPreviewSize(mPreviewSize!!.width, mPreviewSize!!.height);
                params.setPictureSize(mSize!!.width, mSize.height)
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

        if (sizes == null) return null

        var optimalSize: Camera.Size? = null
        val ratio = h.toDouble() / w
        var minDiff = java.lang.Double.MAX_VALUE
        var newDiff: Double
        for (size in sizes) {
            newDiff = Math.abs(size.width.toDouble() / size.height - ratio)
            if (newDiff < minDiff) {
                optimalSize = size
                minDiff = newDiff
            }
        }
        return optimalSize
    }



    override fun surfaceCreated(holder: SurfaceHolder?) {



    }


}


