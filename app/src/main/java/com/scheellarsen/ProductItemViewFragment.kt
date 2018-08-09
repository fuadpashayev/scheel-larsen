package com.scheellarsen


import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_product_item_view.*
import kotlinx.android.synthetic.main.fragment_product_item_view.view.*
import com.google.firebase.database.DataSnapshot
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.json.JSONObject
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.abs_layout.*
import java.io.IOException


class ProductItemViewFragment : Fragment() {
    private var mDatabase : DatabaseReference?=null
    var catId:String?=null
    var scatId:String?=null
    var productId:String?=null
    var imgUrl:String?=null
    var dataCode:String?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val scat_id = this.arguments!!.getString("scid")
        val cat_id = this.arguments!!.getString("cid")
        val product_id = this.arguments!!.getString("id")
        dataCode = this.arguments?.getString("data")
        catId=cat_id
        scatId=scat_id
        productId=product_id

        val rootView = inflater.inflate(R.layout.fragment_product_item_view,container,false)
        activity!!.navigation.visibility = View.GONE
        mDatabase = FirebaseDatabase.getInstance().getReference("detail/$scat_id/$product_id")
        val messageListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val data = dataSnapshot.getValue(Products::class.java)
                    val colors = dataSnapshot.child("colors")
                    val sizes  = dataSnapshot.child("sizes")
                    rootView.findViewById<TextView>(R.id.productName).text = data!!.Name
                    if(imageProduct!=null) {
                        imgUrl=data.Img
                        Glide.with(context)
                                .load(data.Img)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .thumbnail(Glide.with(context).load(R.mipmap.loader))
                                .fitCenter()
                                .crossFade()
                                .into(imageProduct)
                        if(dataCode!=null){
                            val dataCODE = Intent()
                            dataCODE.data = Uri.parse(imgUrl)
                            activity!!.setResult(Activity.RESULT_OK, dataCODE)
                            activity!!.finish()
                        }
                    }

                    if(colors.childrenCount>0 && productColors!=null) {
                        productColors.layoutManager = GridLayoutManager(context, 3)
                        val map = colors.value as HashMap<*, *>
                        val colorList = ArrayList<String>(map.size)
                        for (mapEntry in map.values) {
                            val a = mapEntry.toString().replace("=", ":").replace("value", "'value'").replace(Regex("(#\\w{6})"), "'$1'")
                            val b = JSONObject(a)
                            val c = b["value"].toString()
                            colorList.add(c)
                        }
                        productColors.adapter = ColorAdapter(colorList)
                    }

                    fun dptopx(dp:Int):Int{
                        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.displayMetrics))
                    }

                    if(sizes.childrenCount>0) {
                        val sizeLayout = rootView.findViewById(R.id.productSizes) as LinearLayout
                        for (size in sizes.children) {
                            val sizeName = size.child("value").value as String?
                            val tv = TextView(context)
                            tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            val pr = tv.layoutParams as LinearLayout.LayoutParams
                            pr.setMargins(0,dptopx(4),0,dptopx(5))
                            pr.topMargin = dptopx(4)
                            pr.bottomMargin = dptopx(5)
                            val params = tv.layoutParams
                            params.width = dptopx(113)
                            params.height = dptopx(33)
                            tv.layoutParams = params
                            tv.setTextColor(Color.BLACK)
                            tv.setBackgroundResource(R.drawable.tv_border)
                            tv.gravity = Gravity.CENTER
                            tv.text = sizeName
                            sizeLayout.addView(tv)
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        mDatabase!!.addListenerForSingleValueEvent(messageListener)


        setHasOptionsMenu(true)
        activity!!.backButton.visibility = View.VISIBLE
        rootView.startDialog.setOnClickListener{
            val mAnimals = ArrayList<String>()
            mAnimals.add("Kamera")
            mAnimals.add("Hent billede fra fotogalleri")
            val Animals = mAnimals.toArray(arrayOfNulls<String>(mAnimals.size))
            val dialogBuilder = AlertDialog.Builder(context!!)
            dialogBuilder.setTitle("Valgmuligheder")
            dialogBuilder.setCancelable(true)

            dialogBuilder.setItems(Animals) { _ , item ->
                when(item){
                    0->{
                        val intent = Intent(activity,CameraActivity::class.java)
                        intent.putExtra("imgUrl",imgUrl)
                        intent.putExtra("product_id",productId)
                        intent.putExtra("cat_id",catId)
                        intent.putExtra("scat_id",scatId)
                        startActivity(intent)
                    }
                    1->{
                        val photoPickerIntent = Intent(Intent.ACTION_PICK)
                        photoPickerIntent.type = "image/*"
                        startActivityForResult(photoPickerIntent, 1)
                    }
                }
            }
            val alertDialogObject = dialogBuilder.create()
            alertDialogObject.show()

        }


        activity!!.backButton.setOnClickListener {
            val newFragment = ProductItemFragment()
            val args = Bundle()
            args.putString("id", "$scatId")
            args.putString("cid", "$catId")
            newFragment.arguments = args
            val manager: FragmentManager? = fragmentManager
            val transaction:FragmentTransaction = manager!!.beginTransaction()
            transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
            val tag:String? = newFragment.javaClass.name
            transaction.addToBackStack(tag)
            transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
            activity!!.navigation.visibility = View.VISIBLE
        }

        return rootView

    }



    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {

        if (resultCode == Activity.RESULT_OK && data != null && resultCode!=0){
            when (requestCode) {
                1 -> {
                    val selectedImage = data.data
                    try {
                        val intent = Intent(activity, CameraActivity::class.java)
                        intent.putExtra("imgUrl", imgUrl)
                        intent.putExtra("galleryImage", selectedImage)
                        startActivity(intent)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }











}
