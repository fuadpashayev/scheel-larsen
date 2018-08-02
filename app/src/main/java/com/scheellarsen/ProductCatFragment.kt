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
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.bumptech.glide.Glide


class ProductCatFragment : Fragment() {
    lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase : DatabaseReference
    var MainActivity: MainActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MainActivity = MainActivity()
        val cat_id = this.arguments!!.getString("id")
        val rootView = inflater.inflate(R.layout.fragment_product_cat,container,false)
        mDatabase = FirebaseDatabase.getInstance().getReference("categories/$cat_id")
        mRecyclerView = rootView.findViewById(R.id.listView)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.setLayoutManager(LinearLayoutManager(getContext()))
        logRecyclerView(cat_id)
        (getActivity() as AppCompatActivity).getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean {
        var newFragment = ProductFragment()
        var manager: FragmentManager? = getFragmentManager()
        var transaction:FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out)
        var tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
        (getActivity() as AppCompatActivity).getSupportActionBar()!!.setDisplayHomeAsUpEnabled(false)
        return super.onOptionsItemSelected(item)
    }




    private fun logRecyclerView(cat_id:String) {
        var firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<subCategories, CatsViewHolder>(
                subCategories::class.java,
                R.layout.list_cat_layout,
                CatsViewHolder::class.java,
                mDatabase

        ){

            override fun populateViewHolder(viewHolder: CatsViewHolder?, model: subCategories?, position: Int) {
                viewHolder!!.itemView.subCategoryName.text = model!!.Name
                var imgHolder = viewHolder.itemView.subCatImage
                Glide.with(context)
                        .load(model.Image)
                        .thumbnail(Glide.with(getContext()).load(R.mipmap.loader))
                        .fitCenter()
                        .crossFade()
                        .into(imgHolder)




                var subCatId:String?=model.Index
                loader?.visibility=View.GONE

                viewHolder.itemView.setOnClickListener{
                    loadProductItem(subCatId!!,cat_id!!)
                }

            }

        }
        mRecyclerView.adapter = firebaseRecyclerAdapter
    }


    class CatsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
    fun loadProductItem(sub_cat_id:String,cat_id:String){
        var newFragment = ProductItemFragment()
        val args = Bundle()
        args.putString("id", "$sub_cat_id")
        args.putString("cid", "$cat_id")
        newFragment.arguments = args
        var manager: FragmentManager? = getFragmentManager()
        var transaction:FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,0)
        var tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
    }


}
