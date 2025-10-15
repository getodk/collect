package org.odk.collect.android.mainmenu

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.odk.collect.android.databinding.MinSdkDeprecationBannerBinding
import org.odk.collect.androidshared.data.AppState

class MinSdkDeprecationBanner(private val appState: AppState) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MinSdkDeprecationBannerBinding.inflate(inflater, container, false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && !appState.get(IS_MIN_SDK_DEPRECATION_BANNER_DISMISSED, false)) {
            binding.root.visibility = View.VISIBLE
            binding.dismissButton.setOnClickListener {
                appState.set(IS_MIN_SDK_DEPRECATION_BANNER_DISMISSED, true)
                binding.root.visibility = View.GONE
            }
        }
        return binding.root
    }

    companion object {
        private const val IS_MIN_SDK_DEPRECATION_BANNER_DISMISSED = "isMinSdkDeprecationBannerDismissed"
    }
}
