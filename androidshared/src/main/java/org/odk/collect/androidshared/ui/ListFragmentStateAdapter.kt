package org.odk.collect.androidshared.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ListFragmentStateAdapter(
    activity: FragmentActivity,
    private val fragments: List<Class<out Fragment>>
) : FragmentStateAdapter(activity) {

    private val fragmentFactory = activity.supportFragmentManager.fragmentFactory

    override fun createFragment(position: Int): Fragment {
        return fragmentFactory.instantiate(
            Thread.currentThread().contextClassLoader,
            fragments[position].name
        )
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}
