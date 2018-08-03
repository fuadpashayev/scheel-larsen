package com.scheellarsen

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_layout.*
import kotlinx.android.synthetic.main.list_layout.view.*
import android.support.annotation.NonNull
import android.support.v7.app.ActionBar
import android.view.MenuItem
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*


class MainActivity : AppCompatActivity() {
    var mAuth = FirebaseAuth.getInstance()
    val manager = supportFragmentManager
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_home -> {
               callFragment("Home")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_products -> {
                callFragment("Product")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_about -> {
                callFragment("About")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_contact -> {
                callFragment("Contact")
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        callFragment("Home")
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowHomeEnabled(true)
        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()!!.setCustomView(R.layout.abs_layout);
        setTitle("");
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, object: OnCompleteListener<AuthResult> {
                    override fun onComplete(@NonNull task: Task<AuthResult>) {
                    }
                })
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    fun callFragment(FragmentName:String){
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        val transaction = manager.beginTransaction()
        var fragment = when(FragmentName){
            "Home"-> HomeFragment()
            "Product"-> ProductFragment()
            "About"-> AboutFragment()
            "Contact"-> ContactFragment()
            else-> HomeFragment()
        }
        var currentTag = manager!!.fragments.toString()
        currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag,"$1").trim()
        var newTag = fragment.toString()
        newTag = Regex("(.*)Fragment.*").replace(newTag,"$1")
        if(newTag != currentTag) {
            transaction.setCustomAnimations(R.animator.fade_in, 0)
            var newTag: String? = fragment.javaClass.name
            transaction.replace(R.id.main_frame, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

    override fun onBackPressed() {
        var currentTag = manager!!.fragments.toString()

        if(Regex("AppHelpFragment|ProductCatFragment|ProductItemFragment|ProductItemViewFragment|CameraActivity").find(currentTag)?.value!=null){

            if(Regex("ProductCatFragment").find(currentTag)?.value!=null)
                getSupportActionBar()!!.setDisplayHomeAsUpEnabled(false)

            navigation.visibility = View.VISIBLE
            manager!!.popBackStack()
        }

    }

}

