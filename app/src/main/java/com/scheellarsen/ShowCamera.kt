package com.scheellarsen

import android.content.Context
import android.content.res.Configuration
import android.hardware.Camera
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException


class ShowCamera(context:Context,camera:Camera):SurfaceView(context),SurfaceHolder.Callback {
    lateinit var camera:Camera

    fun ShowCamera(context: Context,camera:Camera){
        super.getContext()
        this.camera = camera
        var holder = holder
        holder.addCallback(this)

    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {

    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        var params:Camera.Parameters = camera.parameters

        if(this.resources.configuration.orientation!= Configuration.ORIENTATION_LANDSCAPE){
            params.set("orientation","portrait")
            camera.setDisplayOrientation(90)
            params.setRotation(90)
        }else{
            params.set("orientation","landscape")
            camera.setDisplayOrientation(0)
            params.setRotation(0)
        }


        camera.parameters = params

        try{
            camera.setPreviewDisplay(holder)

            camera.startPreview()
        }catch (e:IOException){
            e.printStackTrace()
        }






    }





}