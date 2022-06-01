package org.odk.collect.android.configure.qr

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.IllegalArgumentException

class QRCodeTabsAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> QRCodeScannerFragment()
            1 -> ShowQRCodeFragment()
            else -> throw IllegalArgumentException("Fragment position out of bounds")
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}
