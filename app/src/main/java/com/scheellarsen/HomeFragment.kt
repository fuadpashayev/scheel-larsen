package com.scheellarsen


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*


class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater.inflate(R.layout.fragment_home, container, false)
        rootView.info_app.setOnClickListener {
            var newFragment = AppHelpFragment()
            var manager: FragmentManager? = getFragmentManager()
            var transaction:FragmentTransaction = manager!!.beginTransaction()
            transaction.setCustomAnimations(R.animator.fade_in,0)
            var tag:String? = newFragment.javaClass.name
            transaction.addToBackStack(tag)
            transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
        }
        return rootView
    }


}
