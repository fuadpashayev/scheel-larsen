package com.scheellarsen


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.abs_layout.*
import kotlinx.android.synthetic.main.fragment_product_item.*
import kotlinx.android.synthetic.main.list_item_layout.view.*





class ProductItemFragment : Fragment() {
    lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase : DatabaseReference
    var catr_id:String?=null
    var MainActivity: MainActivity? = null
    var data:String?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MainActivity = MainActivity()
        val subCatId = this.arguments!!.getString("id")
        val catId = this.arguments!!.getString("cid")
        data = this.arguments?.getString("data")
        catr_id=catId
        val rootView = inflater.inflate(R.layout.fragment_product_item,container,false)
        mDatabase = FirebaseDatabase.getInstance().getReference("detail/$subCatId")
        mRecyclerView = rootView.findViewById(R.id.listView)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = GridLayoutManager(context,2)
        logRecyclerView(subCatId,catId)
        setHasOptionsMenu(true)
        activity!!.backButton.visibility = View.VISIBLE
        activity!!.backButton.setOnClickListener {
            val newFragment = ProductCatFragment()
            val args = Bundle()
            args.putString("id", "$catr_id")
            newFragment.arguments = args
            val manager: FragmentManager? = getFragmentManager()
            val transaction:FragmentTransaction = manager!!.beginTransaction()
            transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
            val tag:String? = newFragment.javaClass.name
            transaction.addToBackStack(tag)
            transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
        }


        return rootView
    }




    private fun logRecyclerView(subCatId:String,catId:String) {
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Products, ProductsViewHolder>(
                Products::class.java,
                R.layout.list_item_layout,
                ProductsViewHolder::class.java,
                mDatabase

        ){
            override fun getItemCount(): Int {
                return super.getItemCount()
            }

            override fun onDataChanged() {
                val num = itemCount
                 if(num===0){
                     loader!!.visibility=View.GONE
                     productItemError.visibility = View.VISIBLE
               }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
                return super.onCreateViewHolder(parent, viewType)
            }
            override fun populateViewHolder(viewHolder: ProductsViewHolder?, model: Products?, position: Int) {
                loader!!.visibility=View.GONE


                val holder = viewHolder!!.itemView
                holder.productName.text = model!!.Name
                holder.productPrice.text = "${model.Price} DKK"

                Glide.with(context)
                        .load(model.Img)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(Glide.with(context).load(R.mipmap.loader))
                        .fitCenter()
                        .crossFade()
                        .into(holder.itemImage)

                val productId:String?=model.Index


                viewHolder.itemView.setOnClickListener{
                    loadProduct(subCatId,productId!!,catId,data)
                }

            }

        }
        mRecyclerView.adapter = firebaseRecyclerAdapter
    }


    class ProductsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!)

    fun loadProduct(scat_id:String,product_id:String,cat_id:String,data:String?){
        val newFragment = ProductItemViewFragment()
        val args = Bundle()
        args.putString("id", product_id)
        args.putString("scid", scat_id)
        args.putString("cid", cat_id)
        if(data!=null) args.putString("data",data)
        newFragment.arguments = args
        val manager: FragmentManager? = fragmentManager
        val transaction:FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
        val tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
    }


}
