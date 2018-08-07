package com.scheellarsen


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.list_layout.view.*


class ProductFragment : Fragment() {
    lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase : DatabaseReference
    var MainActivity: MainActivity? = null
    var data:String?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MainActivity = MainActivity()
        var rootView = inflater.inflate(R.layout.fragment_products,container,false)
        data = this.arguments?.getString("data")
        mDatabase = FirebaseDatabase.getInstance().getReference("main_categories")
        mRecyclerView = rootView.findViewById(R.id.listView)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.setLayoutManager(LinearLayoutManager(getContext()))
        logRecyclerView()

        return rootView
    }



    private fun logRecyclerView() {
        var firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Categories, CategoriesViewHolder>(
                Categories::class.java,
                R.layout.list_layout,
                CategoriesViewHolder::class.java,
                mDatabase

        ){


            override fun populateViewHolder(viewHolder: CategoriesViewHolder?, model: Categories?, position: Int) {

                viewHolder!!.itemView.catName.text = model!!.Name
                var catId:String?=model.Index!!
                loader?.visibility=View.GONE
                viewHolder.itemView.setOnClickListener{
                    loadProductCat(catId!!,data)
                }

            }

        }
        mRecyclerView.adapter = firebaseRecyclerAdapter
    }


    class CategoriesViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
    fun loadProductCat(cat_id:String,data:String?){
        var newFragment = ProductCatFragment()
        val args = Bundle()
        args.putString("id", "$cat_id")
        if(data!=null) args.putString("data",data)
        newFragment.arguments = args
        var manager: FragmentManager? = getFragmentManager()
        var transaction:FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,0)
        var tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
    }


}
