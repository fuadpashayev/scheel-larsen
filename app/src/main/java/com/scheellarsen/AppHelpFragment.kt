package com.scheellarsen


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.*
import kotlinx.android.synthetic.main.fragment_app_help.*
import kotlinx.android.synthetic.main.fragment_app_help.view.*

class AppHelpFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_app_help, container, false)
        (getActivity() as AppCompatActivity).getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        rootView.imageView32.setClipToOutline(true)
        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean {
        var newFragment = HomeFragment()
        var manager: FragmentManager? = getFragmentManager()
        var transaction: FragmentTransaction = manager!!.beginTransaction()
        transaction.setCustomAnimations(R.animator.fade_in,0)
        var tag:String? = newFragment.javaClass.name
        transaction.addToBackStack(tag)
        transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
        (getActivity() as AppCompatActivity).getSupportActionBar()!!.setDisplayHomeAsUpEnabled(false)
        return super.onOptionsItemSelected(item)
    }

}
