package com.scheellarsen


import android.content.Context
import android.hardware.Camera
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


class CameraActivity : AppCompatActivity() {

    var camera: Camera? = null
    var showCamera:ShowCamera?=null
    var frameLayout: FrameLayout?=null
    val REQUEST_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        frameLayout = findViewById(R.id.camera_frame)

        if(!checkPermission())
            requestPermission()

        if(checkPermission()) {
            showCameraFrame()
        }else
            requestPermission()


    }
    fun showCameraFrame(){
        camera = Camera.open()
        showCamera = ShowCamera(this, camera!!)
        camera_frame!!.addView(showCamera)
        captureImage.setOnClickListener {
            captureImage(camera_frame)
        }
    }
        var mPictureCallback:Camera.PictureCallback = object:Camera.PictureCallback {
            override fun onPictureTaken(data:ByteArray, camera:Camera) {

                var picture_file: File? = getOutputMediaFile()
                if(picture_file==null){
                    return
                }else{
                    try{
                        var fos = FileOutputStream(picture_file)
                        fos.write(data)
                        fos.close()
                        camera.startPreview()
                        Log.d("oldu",picture_file.toString())
                    }catch (e:FileNotFoundException){
                        e.printStackTrace()

                    }


                }
            }
        }



    fun getOutputMediaFile():File?{
        var state:String?=Environment.getExternalStorageState()
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            return null
        }else{
            //var folder_gui:File? = File(Environment.getExternalStorageDirectory(),File.separator+"GUI")
            var folder_gui:File? = File(""+Environment.getExternalStorageDirectory() + File.separator + "GUI")
            if(!folder_gui!!.exists()){
                folder_gui.mkdirs()
            }
            var image = (System.currentTimeMillis()/1000).toString()+"_pic.jpg"
            var imgf:File = File(image)
            var outputFile:File? = File(folder_gui,image)
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imgf)))
            return outputFile
        }
    }


    fun captureImage(v:View){
        if(camera!=null){
            camera!!.takePicture(null,null,mPictureCallback)
        }
    }



    fun checkPermission():Boolean{
        val writeExternalStorage = ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(writeExternalStorage==PackageManager.PERMISSION_GRANTED){
            showCameraFrame()
            return true
        }else
        return false
    }

    fun requestPermission(){
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA),REQUEST_PERMISSION_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQUEST_PERMISSION_CODE->if(grantResults!!.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                showCameraFrame()
            }
        }
    }
}
