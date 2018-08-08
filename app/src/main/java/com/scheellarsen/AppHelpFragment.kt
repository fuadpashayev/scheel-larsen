package com.scheellarsen
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.*
import kotlinx.android.synthetic.main.abs_layout.*
import kotlinx.android.synthetic.main.fragment_app_help.view.*
class AppHelpFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_app_help, container, false)
         setHasOptionsMenu(true)
        rootView.imageView32.clipToOutline = true
        activity!!.backButton.visibility = View.VISIBLE
        activity!!.backButton.setOnClickListener {
            val newFragment = HomeFragment()
            val manager: FragmentManager? = fragmentManager
            val transaction: FragmentTransaction = manager!!.beginTransaction()
            transaction.setCustomAnimations(R.animator.fade_in,0)
            val tag:String? = newFragment.javaClass.name
            transaction.addToBackStack(tag)
            transaction.replace(R.id.main_frame,newFragment,newFragment.tag).commit()
            activity!!.backButton.visibility = View.GONE
        }
        return rootView
    }
}
