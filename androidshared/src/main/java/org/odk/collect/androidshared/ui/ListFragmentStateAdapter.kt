package org.odk.collect.androidshared.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ListFragmentStateAdapter(activity: FragmentActivity, private val fragments: List<String>) :
    FragmentStateAdapter(activity) {

    private val fragmentFactory = activity.supportFragmentManager.fragmentFactory

    override fun createFragment(position: Int): Fragment {
        val className = fragments[position]
        return fragmentFactory.instantiate(Thread.currentThread().contextClassLoader, className)
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}
