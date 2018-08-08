package com.scheellarsen


import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.hardware.Camera
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.*
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.View.*
import android.view.animation.LinearInterpolator
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.abs_layout.*
import java.io.*
import java.util.*



class CameraActivity : AppCompatActivity(){

    private var camera: Camera? = null
    private var showCamera:ShowCamera?=null
    private var frameLayout: FrameLayout?=null
    private val REQUEST_PERMISSION_CODE = 1
    private var opened = 0
    private var image:ImageView?=null
    private var rootLayout:ViewGroup?=null
    private var galleryImage:Any?=null
    private var dCamera:View?=null
    var imageCount:Int=1
    var SGD:GestureDetector?=null

    private var im_move_zoom_rotate:ImageView?=null
    private var scalediff:Float = 0f
    private var NONE = 0
    private var DRAG = 1
    private var ZOOM = 2
    private var mode = NONE
    private var oldDist = 1f
    private var d = 0f
    private var newRot = 0f



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        getSupportActionBar()!!.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val extras = intent.extras
        val imgUrl = extras.get("imgUrl")
        galleryImage = extras.get("galleryImage")



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        frameLayout = findViewById(R.id.camera_frame)
        capture_image.setOnClickListener{
            loader.visibility = View.VISIBLE
            playShutterSound()
            captureImage(camera_frame)
        }
        if(!checkPermission())
            requestPermission()
        if(checkPermission()) {

            if(opened==0 && galleryImage==null) {
                showCameraFrame()

            }else if(opened==0 && galleryImage!=null){
                showGalleryFrame(galleryImage!!)
            }
        }

        rootLayout=findViewById(R.id.rootView)
        image = imageCamera


//        SGD = GestureDetector(image!!.context,gestureDetector())
        Glide.with(this)
            .load(imgUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .thumbnail(Glide.with(this).load(R.mipmap.loader))
            .fitCenter()
            .crossFade()
            .into(image)

        init()

        val layoutParams = FrameLayout.LayoutParams(dptopx(300),dptopx(300))
        val page = rootView
        page.post(object:Runnable {
            override fun run() {
                val width = page.width
                val height = page.height

                val marginL = (width-dptopx(300))/2
                val marginT = (height-dptopx(300))/2

                layoutParams.leftMargin = marginL
                layoutParams.topMargin = marginT


            }
        })


        im_move_zoom_rotate!!.setLayoutParams(layoutParams)
        im_move_zoom_rotate!!.bringToFront()



        im_move_zoom_rotate!!.setOnTouchListener(IMAGETouchListener(im_move_zoom_rotate!!))

        capture_options.setOnClickListener{
            val mAnimals = ArrayList<String>()
            mAnimals.add("Gem billede i fotogalleri")
            mAnimals.add("Tilføj produkt")
            mAnimals.add("Nyt billede")
            val Animals = mAnimals.toArray(arrayOfNulls<String>(mAnimals.size))
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Valgmuligheder")
            dialogBuilder.setCancelable(true)
            dialogBuilder.setItems(Animals, object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface, item:Int) {
                    when(item){
                        0->{
                           takeScreenshot()
                            this@CameraActivity.finish()
                        }
                        1->{
                           val intent = Intent(this@CameraActivity,MainActivity::class.java)
                            intent.putExtra("data","1e9f5t")
                            startActivityForResult(intent,2)
                        }
                        2->{
                            if(galleryImage!=null)
                                this@CameraActivity.finish()
                            else {
                                camera_frame.background = null
                                camera = Camera.open()
                                showCamera = ShowCamera(this@CameraActivity, camera!!)
                                dCamera = showCamera
                                camera_frame!!.addView(showCamera)
                                closeCamera.visibility = View.VISIBLE
                                capture_options.visibility = View.GONE
                                capture_image.visibility = View.VISIBLE
                                opened = 1
                            }
                        }
                    }
                }
            })
            val alertDialogObject = dialogBuilder.create()
            alertDialogObject.show()

        }


        closeCamera.setOnClickListener {
            super.finish()
        }
    }

    override fun onBackPressed() {
        super.finish()
    }


//    inner class gestureDetector:GestureDetector.SimpleOnGestureListener(){
//        override fun onLongPress(e:MotionEvent) {
//
//            if(imageCount>1){
//                Log.d("-------aaa","deyismek olar")
//                Log.d("-------aaa","Silmek olar")
//            }else{
//                Log.d("-------aaa","deyismek olar")
//            }
//
//        }
//    }


    fun playShutterSound(){
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (audio.getRingerMode()) {
            AudioManager.RINGER_MODE_NORMAL -> {
                val sound = MediaActionSound()
                sound.play(MediaActionSound.SHUTTER_CLICK)
            }
            AudioManager.RINGER_MODE_SILENT -> {}
            AudioManager.RINGER_MODE_VIBRATE -> {}
        }
    }

    private inner class IMAGETouchListener(image:ImageView) : OnTouchListener {
        var parms:FrameLayout.LayoutParams?=null
        var startwidth:Int = 0
        var startheight:Int = 0
        var dx = 0f
        var dy = 0f
        var x = 0f
        var y = 0f
        var angle = 0f
        var image = image

        override fun onTouch(v:View, event:MotionEvent):Boolean {
//            SGD!!.onTouchEvent(event)
            val view = image
            val bitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            val b = BitmapDrawable(bitmap)
            b.setAntiAlias(true)
            val duration = event.getEventTime() - event.getDownTime()
            if(duration<200 && event.pointerCount==1 && event.getAction() == 1){
                val sx = image.rotationY
                if(sx==0f){
                    image.rotationY = 180f
                }else{
                    image.rotationY = 0f
                }

            }

            when (event.getAction() and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    parms = view.getLayoutParams() as FrameLayout.LayoutParams
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
                MotionEvent.ACTION_MOVE ->if (mode === DRAG && event.pointerCount===1)
                {
                    x = event.getRawX()
                    y = event.getRawY()
                    parms!!.leftMargin = (x - dx).toInt()
                    parms!!.topMargin = (y - dy).toInt()
                    parms!!.rightMargin = 0
                    parms!!.bottomMargin = 0
                    parms!!.rightMargin = parms!!.leftMargin + (5 * parms!!.width)
                    parms!!.bottomMargin = parms!!.topMargin + (10 * parms!!.height)
                    view.setLayoutParams(parms)
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
                            val scale = newDist / oldDist * view.getScaleX()
                            if (scale > 0.6)
                            {
                                scalediff = scale
                                view.setScaleX(scale)
                                view.setScaleY(scale)
                            }
                        }
                        view.animate().rotationBy(angle).setDuration(0).setInterpolator(LinearInterpolator()).start()

                        view.setLayoutParams(parms)
                    }
                }else if(mode!=ZOOM && mode!=DRAG){
                    if(event.pointerCount==2){
                        view.animate().rotationBy(angle).setDuration(0).setInterpolator(LinearInterpolator()).start()
                    }

                }
            }
            return true
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val img = data!!.data
        val IMAGE = ImageView(this)
        val layoutParams = FrameLayout.LayoutParams(dptopx(200),dptopx(200))
        val page = rootView
        page.post(object:Runnable {
            override fun run() {
                val width = page.width
                val height = page.height

                val marginL = (width-dptopx(200))/2
                val marginT = (height-dptopx(200))/2

                layoutParams.leftMargin = marginL
                layoutParams.topMargin = marginT

            }
        })


        IMAGE.setLayoutParams(layoutParams)
        IMAGE.bringToFront()


        Glide.with(this)
                .load(img)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(Glide.with(this).load(R.mipmap.loader))
                .fitCenter()
                .crossFade()
                .into(IMAGE)
        rootView.addView(IMAGE)
        imageCount++
        IMAGE.setOnTouchListener(IMAGETouchListener(IMAGE))

    }


    fun showGalleryFrame(backImage:Any) {
        val img = backImage.toString()
        val selectedPicture = Uri.parse(img)
        val filePathColumn = arrayOf<String>(MediaStore.Images.Media.DATA)
        val cursor = this.getContentResolver().query(selectedPicture, filePathColumn, null, null, null)
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        val picturePath = cursor.getString(columnIndex)
        cursor.close()
        var loadedBitmap = BitmapFactory.decodeFile(picturePath)
        var exif: ExifInterface? = null
        try
        {
            val pictureFile = File(picturePath)
            exif = ExifInterface(pictureFile.getAbsolutePath())
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        var orientation = ExifInterface.ORIENTATION_NORMAL
        if (exif != null)
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> loadedBitmap = rotateBitmap(loadedBitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> loadedBitmap = rotateBitmap(loadedBitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> loadedBitmap = rotateBitmap(loadedBitmap, 270)
        }

        var d = BitmapDrawable(resources,loadedBitmap)
        camera_frame.background = d
        capture_image.visibility = View.GONE
        capture_options.visibility = View.VISIBLE
        closeCamera.visibility = View.GONE
    }
    fun rotateBitmap(bitmap:Bitmap, degrees:Int):Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)
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

    override fun onRestart() {
        opened=1
        super.onRestart()
    }
    fun showCameraFrame(){
        if(opened==0 && galleryImage==null) {
            camera = Camera.open()
            showCamera = ShowCamera(this, camera!!)
            dCamera = showCamera
            camera_frame!!.addView(showCamera)
            closeCamera.visibility = View.VISIBLE
            opened = 1
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


                        val exif = ExifInterface(Uri.parse(picture_file.toString()).path)
                        val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)
                        val rotationDegrees = exifToDegrees(rotation)
                        val matrix = Matrix()
                        if(rotation!=0) matrix.preRotate(rotationDegrees.toFloat());
                        val file = File(picture_file.path)
                        val uri = Uri.fromFile(file)
                        val bitmap = MediaStore.Images.Media.getBitmap(this@CameraActivity.contentResolver,uri)
                        val a = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true)
                        camera_frame.removeView(dCamera)
                        camera_frame.background = BitmapDrawable(resources,a)
                        loader.visibility = View.GONE
                        capture_image.visibility = View.GONE
                        capture_options.visibility = View.VISIBLE
                        closeCamera.visibility = View.GONE
                        file.delete()

                        opened=1
                    }catch (e:FileNotFoundException){
                        e.printStackTrace()

                    }


                }
            }
        }

    private fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    fun getOutputMediaFile():File?{
        val state:String?=Environment.getExternalStorageState()
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            return null
        }else{
            val folder_gui:File? = File(""+Environment.getExternalStorageDirectory() + File.separator + "Scheellarsen")
            if(!folder_gui!!.exists()){
                folder_gui.mkdirs()
            }
            val image = "larsen_"+(System.currentTimeMillis()/1000).toString()+".jpg"
            val outputFile:File? = File(folder_gui,image)
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)))
            return outputFile
        }
    }


    fun captureImage(v:View){

        if(camera!=null){
            camera!!.takePicture(null,null,mPictureCallback)
        }else{
            takeScreenshot()
        }
    }

    private fun takeScreenshot() {
        try
        {
            val folder_gui:File? = File(""+Environment.getExternalStorageDirectory() + File.separator + "Scheellarsen")
            if(!folder_gui!!.exists()){
                folder_gui.mkdirs()
            }
            val mPath= "larsen_"+(System.currentTimeMillis()/1000).toString()+".jpg"
            val v1 = rootView
            v1.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(v1.getDrawingCache())
            v1.isDrawingCacheEnabled = false
            val imageFile = File(folder_gui,mPath)
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)))
            Toast.makeText(this,"Picture added to Gallery",Toast.LENGTH_LONG).show()

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
            REQUEST_PERMISSION_CODE->if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(opened==0)
                    showCameraFrame()
            }
        }
    }
}
