package com.scheellarsen


import android.annotation.SuppressLint
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
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.util.FloatMath
import android.util.TypedValue
import android.view.*
import android.view.View.*
import android.view.animation.LinearInterpolator
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.muddzdev.viewshotlibrary.Viewshot
import kotlinx.android.synthetic.main.fragment_product_item_view.*
import java.security.AccessController.getContext
import java.util.*


class CameraActivity : AppCompatActivity(){

    var camera: Camera? = null
    var showCamera:ShowCamera?=null
    var frameLayout: FrameLayout?=null
    val REQUEST_PERMISSION_CODE = 1
    var opened = 0
    var image:ImageView?=null
    var rootLayout:ViewGroup?=null

    var im_move_zoom_rotate:ImageView?=null
    var toolbar:android.support.v7.widget.Toolbar?=null
    var scalediff:Float = 0f
    var NONE = 0
    var DRAG = 1
    var ZOOM = 2
    var mode = NONE
    var oldDist = 1f
    var d = 0f
    var newRot = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        val extras = intent.extras
        var imgUrl = extras.get("imgUrl")
        var galleryImage = BitmapFactory.decodeStream(this.openFileInput("myImage"))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        frameLayout = findViewById(R.id.camera_frame)
        capture_image.setOnClickListener{
            captureImage(camera_frame)
        }
        if(!checkPermission())
            requestPermission()
        if(checkPermission()) {

            if(opened==0 && galleryImage==null) {
                showCameraFrame()

            }else{
                showGalleryFrame(galleryImage)
            }
        }
        rootLayout=findViewById(R.id.rootView)
        image = imageCamera


        Glide.with(this)
            .load(imgUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .thumbnail(Glide.with(this).load(R.mipmap.loader))
            .fitCenter()
            .crossFade()
            .into(image)

        init()

        var layoutParams = FrameLayout.LayoutParams(dptopx(200),dptopx(200))
        //layoutParams.gravity=Gravity.CENTER
        var page = rootView
        page.post(object:Runnable {
            override fun run() {
                var width = page.width
                var height = page.height

                var marginL = (width-dptopx(200))/2
                var marginT = (height-dptopx(200))/2

                layoutParams.leftMargin = marginL
                layoutParams.topMargin = marginT

            }
        })



        //layoutParams.rightMargin = 50


       // layoutParams.bottomMargin = 50


        im_move_zoom_rotate!!.setLayoutParams(layoutParams)
        im_move_zoom_rotate!!.bringToFront()


        im_move_zoom_rotate!!.setOnTouchListener(object:View.OnTouchListener {
            var parms:FrameLayout.LayoutParams?=null
            var startwidth:Int = 0
            var startheight:Int = 0
            var dx = 0f
            var dy = 0f
            var x = 0f
            var y = 0f
            var angle = 0f
            override fun onTouch(v:View, event:MotionEvent):Boolean {
                var view = image
                var bitmap = Bitmap.createBitmap(view!!.width,view!!.height,Bitmap.Config.ARGB_8888)
                var b = BitmapDrawable(bitmap)
                b.setAntiAlias(true)
                var duration = event.getEventTime() - event.getDownTime()
                if(duration<200 && event.pointerCount==1 && event.getAction() == 1){
                    var sx = image!!.rotationY
                    if(sx==0f){
                        image!!.rotationY = 180f
                    }else{
                        image!!.rotationY = 0f
                    }

                }
                when (event.getAction() and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        parms = view!!.getLayoutParams() as FrameLayout.LayoutParams
                        startwidth = parms!!.width
                        startheight = parms!!.height
                        dx = event.getRawX() - parms!!.leftMargin
                        dy = event.getRawY() - parms!!.topMargin
                        mode = DRAG
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        oldDist = spacing(event)
                        if (oldDist > 10f)
                        {
                            mode = ZOOM
                        }
                        d = rotation(event)
                    }
                    MotionEvent.ACTION_UP -> {}
                    MotionEvent.ACTION_POINTER_UP -> mode = NONE
                    MotionEvent.ACTION_MOVE -> if (mode === DRAG && event.pointerCount===1)
                    {
                        x = event.getRawX()
                        y = event.getRawY()
                        parms!!.leftMargin = (x - dx).toInt()
                        parms!!.topMargin = (y - dy).toInt()
                        parms!!.rightMargin = 0
                        parms!!.bottomMargin = 0
                        parms!!.rightMargin = parms!!.leftMargin + (5 * parms!!.width)
                        parms!!.bottomMargin = parms!!.topMargin + (10 * parms!!.height)
                        view!!.setLayoutParams(parms)
                    }
                    else if (mode === ZOOM)
                    {
                        if (event.pointerCount === 2)
                        {
                            newRot = rotation(event)
                            val r = newRot - d
                            angle = r
                            x = event.getRawX()
                            y = event.getRawY()
                            val newDist = spacing(event)
                            if (newDist > 10f)
                            {
                                val scale = newDist / oldDist * view!!.getScaleX()
                                if (scale > 0.6)
                                {
                                    scalediff = scale
                                    view!!.setScaleX(scale)
                                    view!!.setScaleY(scale)
                                }
                            }
                            view!!.animate().rotationBy(angle).setDuration(0).setInterpolator(LinearInterpolator()).start()


                            x = event.getRawX()
                            y = event.getRawY()
                            parms!!.leftMargin = ((x - dx) + scalediff).toInt()
                            parms!!.topMargin = ((y - dy) + scalediff).toInt()
                            parms!!.rightMargin = 0
                            parms!!.bottomMargin = 0
                            parms!!.rightMargin = parms!!.leftMargin + (5 * parms!!.width)
                            parms!!.bottomMargin = parms!!.topMargin + (10 * parms!!.height)

                            view.setLayoutParams(parms)
                        }
                    }
                }
                return true
            }
        })

    }

    fun showGalleryFrame(backImage:Bitmap) {
       Log.d("geldiii","ashahshashahs")
    }


    fun init() {
        im_move_zoom_rotate = image
    }
    fun spacing(event:MotionEvent):Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }
    private fun rotation(event:MotionEvent):Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }








    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.getDisplayMetrics()));
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
//            var bitmap = Bitmap.createBitmap(camera_frame.width,camera_frame.height,Bitmap.Config.ARGB_8888)
//            var handlerThread = HandlerThread("PixelCopier")
//            handlerThread.start()
//            //var surface:Surface = camera_frame as Surface
//            PixelCopy.request(rootView!!,bitmap,{
//                Log.d("--------a",it.toString())
//            },Handler(handlerThread.getLooper()))
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
            camera_frame.setBackgroundDrawable(canvas as Drawable)
            v1.setDrawingCacheEnabled(false)
            val imageFile = File(mPath)
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.PNG,  quality, outputStream)
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
