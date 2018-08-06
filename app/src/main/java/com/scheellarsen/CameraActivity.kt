package com.scheellarsen


import android.annotation.SuppressLint
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.DialogInterface
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
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.MotionEventCompat
import android.support.v7.app.AlertDialog
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
import java.io.IOException
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
    var galleryImage:Any?=null

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
        galleryImage = extras.get("galleryImage")



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

            }else if(opened==0 && galleryImage!=null){
                showGalleryFrame(galleryImage!!)
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

                            view.setLayoutParams(parms)
                        }
                    }else if(mode!=ZOOM && mode!=DRAG){
                        if(event.pointerCount==2){
                            view!!.animate().rotationBy(angle).setDuration(0).setInterpolator(LinearInterpolator()).start()
                        }

                    }
                }
                return true
            }
        })
        capture_options.setOnClickListener{
            val mAnimals = ArrayList<String>()
            mAnimals.add("Save")
            mAnimals.add("Add Product")
            val Animals = mAnimals.toArray(arrayOfNulls<String>(mAnimals.size))
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Valgmuligheder")
            dialogBuilder.setCancelable(true)
            dialogBuilder.setItems(Animals, object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface, item:Int) {
                    when(item){
                        0->{
                           takeScreenshot()
                        }
                        1->{
                           return
                        }
                    }
                }
            })
            val alertDialogObject = dialogBuilder.create()
            alertDialogObject.show()

        }
    }

    fun showGalleryFrame(backImage:Any) {
        var img = backImage.toString()
        var selectedPicture = Uri.parse(img)
        var filePathColumn = arrayOf<String>(MediaStore.Images.Media.DATA)
        var cursor = this.getContentResolver().query(selectedPicture, filePathColumn, null, null, null)
        cursor.moveToFirst()
        var columnIndex = cursor.getColumnIndex(filePathColumn[0])
        var picturePath = cursor.getString(columnIndex)
        cursor.close()
        var loadedBitmap = BitmapFactory.decodeFile(picturePath)
        var exif: ExifInterface? = null
        try
        {
            var pictureFile = File(picturePath)
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
            camera_frame!!.addView(showCamera)
            //var image = ImageView(this)
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
                        camera_frame.removeAllViews()

                        val exif = ExifInterface(Uri.parse(picture_file.toString()).path)
                        val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)
                        val rotationDegrees = exifToDegrees(rotation)
                        val matrix = Matrix()
                        if(rotation!=0) matrix.preRotate(rotationDegrees.toFloat());
                        val file = File(picture_file.path)
                        val uri = Uri.fromFile(file)
                        val bitmap = MediaStore.Images.Media.getBitmap(this@CameraActivity.contentResolver,uri)
                        val a = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true)
                        camera_frame.background = BitmapDrawable(resources,a)
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
            capture_image.visibility = View.GONE
            capture_options.visibility = View.VISIBLE

        }else{
            takeScreenshot()
        }
    }

    private fun takeScreenshot() {
        try
        {
            // image naming and path to include sd card appending name you choose for file

            var folder_gui:File? = File(""+Environment.getExternalStorageDirectory() + File.separator + "Scheellarsen")
            if(!folder_gui!!.exists()){
                folder_gui.mkdirs()
            }
            val mPath= "larsen_"+(System.currentTimeMillis()/1000).toString()+".jpg"
            // create bitmap screen capture
            val v1 = rootView
            v1.setDrawingCacheEnabled(true)
            val bitmap = Bitmap.createBitmap(v1.getDrawingCache())
            v1.setDrawingCacheEnabled(false)
            val imageFile = File(folder_gui,mPath)
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)))
            Toast.makeText(this,"Picture added to Gallery",Toast.LENGTH_LONG).show()
            Handler().postDelayed({
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(Uri.parse(imageFile.toString()),"image/*")
                startActivity(intent)
            },1000)

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
