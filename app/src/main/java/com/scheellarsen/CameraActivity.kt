package com.scheellarsen


import android.app.PendingIntent.getActivity
import android.content.Context
import android.hardware.Camera
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.util.FloatMath
import android.util.TypedValue
import android.view.*
import android.view.View.OnTouchListener
import android.view.View.generateViewId
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_product_item_view.*
import java.security.AccessController.getContext
import java.util.*


class CameraActivity : AppCompatActivity() {

    var camera: Camera? = null
    var showCamera:ShowCamera?=null
    var frameLayout: FrameLayout?=null
    val REQUEST_PERMISSION_CODE = 1
    var opened = 0

    var _xDelta: Int = 0
    var _yDelta: Int = 0
    var imgScale:ImageView?=null
    var matrix:Matrix = Matrix()
    var scale:Float =1f
    var SGD:ScaleGestureDetector?=null

    var rootLayout:ViewGroup?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        val extras = intent.extras
        var imgUrl = extras.get("imgUrl")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        frameLayout = findViewById(R.id.camera_frame)
        capture_image.setOnClickListener{
            captureImage(camera_frame)
        }
        if(!checkPermission())
            requestPermission()
        if(checkPermission()) {
            if(opened==0)
                showCameraFrame()
        }
        rootLayout=findViewById(R.id.camera_frame)



        var image = ImageView(this)
        var imageParams = RelativeLayout.LayoutParams(dptopx(150),dptopx(150))
        image.layoutParams = imageParams
                //image.setImageResource(R.mipmap.img2_apphelp)
       // image.scaleType = ImageView.ScaleType.MATRIX
        camera_frame.addView(image)

                Glide.with(this)
                .load(imgUrl)
                .thumbnail(Glide.with(this).load(R.mipmap.loader))
                .fitCenter()
                .crossFade()
                .into(image)

        var img = image
        val layoutParams = FrameLayout.LayoutParams(350, 350)
        img.setLayoutParams(layoutParams)
        img.setOnTouchListener(ChoiceTouchListener())




        imgScale = image
        SGD = ScaleGestureDetector(camera_frame.context,ScaleListener())

    }
    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.getDisplayMetrics()));
    }

    inner class ScaleListener():ScaleGestureDetector.SimpleOnScaleGestureListener(){
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            scale = scale*detector!!.scaleFactor
            scale = Math.max(0.1f,Math.min(scale,10f))
            imgScale!!.setScaleX(scale)
            imgScale!!.setScaleY(scale)
            imgScale!!.setImageMatrix(matrix)
            Log.d("-------aaa","aaaaaaaaaaa")
            return true

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        SGD!!.onTouchEvent(event)
        return true

    }


    private inner class ChoiceTouchListener : OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val X = event.rawX.toInt()
            val Y = event.rawY.toInt()
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    val lParams = view.layoutParams as FrameLayout.LayoutParams
                    _xDelta = X - lParams.leftMargin
                    _yDelta = Y - lParams.topMargin
                }
                MotionEvent.ACTION_UP -> {
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                }
                MotionEvent.ACTION_POINTER_UP -> {
                }
                MotionEvent.ACTION_MOVE -> {
                    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                    layoutParams.leftMargin = X - _xDelta
                    layoutParams.topMargin = Y - _yDelta
                    layoutParams.rightMargin = -250
                    layoutParams.bottomMargin = -250
                    view.layoutParams = layoutParams
                }
            }
            rootLayout!!.invalidate()
            return true
        }
    }

    fun showCameraFrame(){
        camera = Camera.open()
        showCamera = ShowCamera(this, camera!!)
        camera_frame!!.addView(showCamera)
        //var image = ImageView(this)
        opened=1

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
            var folder_gui:File? = File(""+Environment.getExternalStorageDirectory() + File.separator + "Scheellarsen")
            if(!folder_gui!!.exists()){
                folder_gui.mkdirs()
            }
            var image = "larsen_"+(System.currentTimeMillis()/1000).toString()+".jpg"
            var imgf:File = File(image)
            var outputFile:File? = File(folder_gui,image)
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)))
            return outputFile
        }
    }


    fun captureImage(v:View){
        if(camera!=null){
            camera!!.takePicture(null,null,mPictureCallback)
            takeScreenshot()
        }
    }

    private fun takeScreenshot() {
        val now = Date()
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        try
        {
            val mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"
            val v1 = camera_frame
            v1.setDrawingCacheEnabled(true)
            var surfaceViewDrawingCache = v1.getDrawingCache()
            val bitmap = Bitmap.createBitmap(v1.getWidth(), v1.getHeight(),Bitmap.Config.ARGB_8888)
            var canvas: Canvas = Canvas(bitmap)
            camera_frame.draw(canvas)
            v1.setDrawingCacheEnabled(false)
            val imageFile = File(mPath)
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
        }
        catch (e:Throwable) {
            e.printStackTrace()
        }
    }



    fun checkPermission():Boolean{
        val writeExternalStorage = ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val useCamera = ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)
        if(writeExternalStorage==PackageManager.PERMISSION_GRANTED && useCamera==PackageManager.PERMISSION_GRANTED){
            if(opened==0)
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
                if(opened==0)
                    showCameraFrame()
            }
        }
    }
}
