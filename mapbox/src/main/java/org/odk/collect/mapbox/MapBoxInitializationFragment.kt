package org.odk.collect.mapbox

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.startup.AppInitializer
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.loader.MapboxMapsInitializer
import org.odk.collect.androidshared.data.getState
import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.shared.injection.ObjectProviderHost

class MapBoxInitializationFragment : Fragment() {

    private val settingsProvider: SettingsProvider by lazy {
        (requireActivity().applicationContext as ObjectProviderHost).getObjectProvider().provide(SettingsProvider::class.java)
    }

    private val connectivityProvider: NetworkStateProvider by lazy {
        (requireActivity().applicationContext as ObjectProviderHost).getObjectProvider().provide(NetworkStateProvider::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeMapbox(context)
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

    private fun initializeMapbox(context: Context) {
        // Auto initialization is disabled to stop x86 devices from crashing so we do this manually
        context.getState().let {
            if (!it.get(KEY_MAPBOX_INITIALIZED, false)) {
                AppInitializer.getInstance(context)
                    .initializeComponent(MapboxMapsInitializer::class.java)
                it.set(KEY_MAPBOX_INITIALIZED, true)
            }
        }
    }

    companion object {
        private const val KEY_MAPBOX_INITIALIZED = "mapbox_initialized"
    }
}
