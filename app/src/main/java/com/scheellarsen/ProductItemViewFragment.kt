package com.scheellarsen


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_product_item_view.*
import kotlinx.android.synthetic.main.fragment_product_item_view.view.*
import com.google.firebase.database.DataSnapshot
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item_layout.view.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.json.JSONObject




class ProductItemViewFragment : Fragment() {
    lateinit var mRecyclerView: RecyclerView
    public lateinit var mDatabase : DatabaseReference
    var MainActivity: MainActivity? = null
    var catId:String?=null
    var scatId:String?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MainActivity = MainActivity()
        val scat_id = this.arguments!!.getString("scid")
        val cat_id = this.arguments!!.getString("cid")
        val product_id = this.arguments!!.getString("id")
        catId=cat_id
        scatId=scat_id
        val rootView = inflater.inflate(R.layout.fragment_product_item_view,container,false)
        activity!!.navigation.visibility = View.GONE
        mDatabase = FirebaseDatabase.getInstance().getReference("detail/$scat_id/$product_id")
        //var message:com.scheellarsen.Products?=null
        val messageListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var data = dataSnapshot.getValue(Products::class.java)
                    val colors = dataSnapshot.child("colors")
                    val sizes  = dataSnapshot.child("sizes")
                    productName.text = data!!.Name
                    Glide.with(context)
                            .load(data!!.Img)
                            .thumbnail(Glide.with(getContext()).load(R.mipmap.loader))
                            .fitCenter()
                            .crossFade()
                            .into(imageProduct)


                    if(colors.childrenCount>0) {
                        productColors.layoutManager = GridLayoutManager(context, 3)
                        var mapp = colors.getValue() as HashMap<String, Any>
                        val map = mapp
                        val colorList = ArrayList<String>(map.size)
                        for (mapEntry in map.values) {
                            var a = mapEntry.toString().replace("=", ":").replace("value", "'value'").replace(Regex("(\\#\\w{6})"), "'$1'")
                            var b = JSONObject(a)
                            var c = b["value"].toString()
                            colorList.add(c)
                        }
                        productColors.adapter = ColorAdapter(colorList)
                    }

                    fun dptopx(dp:Int):Int{
                        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.getDisplayMetrics()));
                    }

                    if(sizes.childrenCount>0) {
                        val sizeLayout = rootView.findViewById(R.id.productSizes) as LinearLayout
                        for (size in sizes.children) {
                            val sizeName = size.child("value").value as String?
                            var tv: TextView = TextView(context)
                            tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            var pr = tv.getLayoutParams() as LinearLayout.LayoutParams
                            pr.setMargins(0,dptopx(4),0,dptopx(5))
                            pr.topMargin = dptopx(4)
                            pr.bottomMargin = dptopx(5)
                            val params = tv.getLayoutParams()
                            params.width = dptopx(113)
                            params.height = dptopx(33)
                            tv.setLayoutParams(params)
                            tv.setTextColor(Color.BLACK)
                            tv.setBackgroundResource(R.drawable.tv_border)
                            tv.gravity = Gravity.CENTER
                            tv.setText(sizeName)
                            sizeLayout.addView(tv)
                        }
                    }





                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        mDatabase!!.addListenerForSingleValueEvent(messageListener)

        (getActivity() as AppCompatActivity).getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        rootView.startCameraView.setOnClickListener{
            val intent = Intent(activity,CameraActivity::class.java)
            startActivity(intent)
        }

        return rootView

    }




    override fun onOptionsItemSelected(item: MenuItem):Boolean {
        var newFragment = ProductItemFragment()
        val args = Bundle()
        args.putString("id", "$scatId")
        args.putString("cid", "$catId")
        newFragment.arguments = args
        var manager: FragmentManager? = getFragmentManager()
        var transaction:FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
        var tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
        activity!!.navigation.visibility = View.VISIBLE
        return super.onOptionsItemSelected(item)
    }







}
