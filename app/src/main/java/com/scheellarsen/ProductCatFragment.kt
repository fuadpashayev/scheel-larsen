package com.scheellarsen
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_product_cat.*
import kotlinx.android.synthetic.main.list_cat_layout.view.*
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.abs_layout.*

class ProductCatFragment : Fragment() {
    private lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase : DatabaseReference
    var data:String?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val cat_id = this.arguments!!.getString("id")
        data = this.arguments?.getString("data")
        val rootView = inflater.inflate(R.layout.fragment_product_cat,container,false)
        mDatabase = FirebaseDatabase.getInstance().getReference("categories/$cat_id")
        mRecyclerView = rootView.findViewById(R.id.listView)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        logRecyclerView(cat_id)
        setHasOptionsMenu(true)
        activity!!.backButton.visibility = View.VISIBLE
        activity!!.backButton.setOnClickListener {
            val newFragment = ProductFragment()
            val manager: FragmentManager? = fragmentManager
            val transaction:FragmentTransaction = manager!!.beginTransaction()
            transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
            val tag:String? = newFragment.javaClass.name
            transaction.addToBackStack(tag)
            transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
            activity!!.backButton.visibility = View.GONE
        }
        return rootView
    }

    private fun logRecyclerView(cat_id:String) {
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<subCategories, CatsViewHolder>(
                subCategories::class.java,
                R.layout.list_cat_layout,
                CatsViewHolder::class.java,
                mDatabase
        ){
            override fun populateViewHolder(viewHolder: CatsViewHolder?, model: subCategories?, position: Int) {
                viewHolder!!.itemView.subCategoryName.text = model!!.Name
                val imgHolder = viewHolder.itemView.subCatImage
                Glide.with(context)
                        .load(model.Image)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .thumbnail(Glide.with(context).load(R.mipmap.loader))
                        .fitCenter()
                        .crossFade(1000)
                        .into(imgHolder)
                val subCatId:String?=model.Index
                loader?.visibility=View.GONE
                viewHolder.itemView.setOnClickListener{
                    loadProductItem(subCatId!!,cat_id,data)
                }
            }
        }
        mRecyclerView.adapter = firebaseRecyclerAdapter
    }

    class CatsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!)
    fun loadProductItem(sub_cat_id:String,cat_id:String,data:String?){
        val newFragment = ProductItemFragment()
        val args = Bundle()
        args.putString("id", sub_cat_id)
        args.putString("cid", cat_id)
        if(data!=null) args.putString("data",data)
        newFragment.arguments = args
        val manager: FragmentManager? = fragmentManager
        val transaction:FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,0)
        val tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
    }
}
