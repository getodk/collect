package org.odk.collect.android.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys
import javax.inject.Inject

class MapBoxInitializationFragment : Fragment() {
    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var connectivityProvider: NetworkStateProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.mapbox_fragment_layout, container, false)
        initMapBox(rootView)
        return rootView
    }

    private fun initMapBox(rootView: View) {
        val metaSharedPreferences = settingsProvider.getMetaSettings()
        if (!metaSharedPreferences.getBoolean(MetaKeys.KEY_MAPBOX_INITIALIZED) && connectivityProvider.isDeviceOnline) {
            // This "one weird trick" lets us initialize MapBox at app start when the internet is
            // most likely to be available. This is annoyingly needed for offline tiles to work.
            try {
                val mapView = MapView(requireContext())
                val mapBoxContainer = rootView.findViewById<FrameLayout>(R.id.map_box_container)
                mapBoxContainer.addView(mapView)
                mapView.getMapboxMap().loadStyleUri(
                    Style.MAPBOX_STREETS
                ) {
                    metaSharedPreferences.save(
                        MetaKeys.KEY_MAPBOX_INITIALIZED,
                        true
                    )
                }
            } catch (ignored: Throwable) {
                // This will crash on devices where the arch for MapBox is not included
            }
        }
    }
}
