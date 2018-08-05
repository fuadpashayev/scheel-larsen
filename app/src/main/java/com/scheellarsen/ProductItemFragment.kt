package com.scheellarsen


import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_product_item.*
import kotlinx.android.synthetic.main.list_item_layout.view.*





class ProductItemFragment : Fragment() {
    lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase : DatabaseReference
    var catr_id:String?=null
    var MainActivity: MainActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MainActivity = MainActivity()
        val subCatId = this.arguments!!.getString("id")
        val catId = this.arguments!!.getString("cid")
        catr_id=catId
        val rootView = inflater.inflate(R.layout.fragment_product_item,container,false)
        mDatabase = FirebaseDatabase.getInstance().getReference("detail/$subCatId")
        var mData = mDatabase
        mRecyclerView = rootView.findViewById(R.id.listView)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.setLayoutManager(GridLayoutManager(context,2))
        logRecyclerView(subCatId,catId)
        (getActivity() as AppCompatActivity).getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean {
        var newFragment = ProductCatFragment()
        val args = Bundle()
        args.putString("id", "$catr_id")
        newFragment.arguments = args
        var manager: FragmentManager? = getFragmentManager()
        var transaction:FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
        var tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
        return super.onOptionsItemSelected(item)
    }



    private fun logRecyclerView(subCatId:String,catId:String) {
        var firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Products, ProductsViewHolder>(
                Products::class.java,
                R.layout.list_item_layout,
                ProductsViewHolder::class.java,
                mDatabase

        ){
            private val products:Array<Products> = arrayOf()
            override fun getItemCount(): Int {
                return super.getItemCount()
            }

            override fun onDataChanged() {
                var num = itemCount
                 if(num===0){
                     loader!!.visibility=View.GONE
                     productItemError.visibility = View.VISIBLE
                    Handler().postDelayed({
                        var fragment = ProductCatFragment()
                        val args = Bundle()
                        args.putString("id", "$catId")
                        fragment.arguments = args
                        var manager: FragmentManager? = getFragmentManager()
                        var transaction:FragmentTransaction = manager!!.beginTransaction()
                        transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
                        var tag:String? = fragment.javaClass.name
                        transaction.addToBackStack(tag)
                        transaction.replace(R.id.main_frame,fragment,fragment.tag).commit()

                   },5000)
               }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
                return super.onCreateViewHolder(parent, viewType)
            }
            override fun populateViewHolder(viewHolder: ProductsViewHolder?, model: Products?, position: Int) {
                loader!!.visibility=View.GONE


                val holder = viewHolder!!.itemView
                holder.productName.text = model!!.Name
                holder.productPrice.text = """${model.Price} DKK"""

                Glide.with(context)
                        .load(model.Img)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(Glide.with(getContext()).load(R.mipmap.loader))
                        .fitCenter()
                        .crossFade()
                        .into(holder.itemImage)

                val productId:String?=model.Index


                viewHolder.itemView.setOnClickListener{
                   // Log.d("------id",subCatId+" - "+productId+" - "+catId)
                    loadProduct(subCatId,productId!!,catId)
                }

            }

        }
        mRecyclerView.adapter = firebaseRecyclerAdapter
    }


    class ProductsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }

    fun loadProduct(scat_id:String,product_id:String,cat_id:String){
        var newFragment = ProductItemViewFragment()
        val args = Bundle()
        args.putString("id", "$product_id")
        args.putString("scid", "$scat_id")
        args.putString("cid", "$cat_id")
        newFragment.arguments = args
        var manager: FragmentManager? = getFragmentManager()
        var transaction:FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
        var tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
    }


}
