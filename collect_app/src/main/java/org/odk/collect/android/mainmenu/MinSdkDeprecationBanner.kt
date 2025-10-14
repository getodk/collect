package org.odk.collect.android.mainmenu

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.odk.collect.android.databinding.MinSdkDeprecationBannerBinding

class MinSdkDeprecationBanner : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MinSdkDeprecationBannerBinding.inflate(inflater, container, false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && !isMinSdkDeprecationBannerDismissed) {
            binding.root.visibility = View.VISIBLE
            binding.dismissButton.setOnClickListener {
                isMinSdkDeprecationBannerDismissed = true
                binding.root.visibility = View.GONE
            }
        }
        return binding.root
    }

    companion object {
        var isMinSdkDeprecationBannerDismissed = false
    }
}
