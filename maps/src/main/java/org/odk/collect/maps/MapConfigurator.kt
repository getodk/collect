package org.odk.collect.maps

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import org.odk.collect.shared.settings.Settings
import java.io.File

/**
 * For each MapFragment implementation class, there is one instance of this
 * interface that encapsulates the details of how to configure that MapFragment
 * and reconfigure it as settings are changed.  In particular, the buildConfig()
 * method builds a Bundle object containing configuration details, which the
 * MapFragment.applyConfig() method should understand.  Each MapConfigurator can
 * define its own preference widgets for customizing the MapFragment, and
 * those preferences will be inserted on the Maps preference screen.
 * For example, the GoogleMapConfigurator can define a "Google map style"
 * preference with choices such as Terrain or Satellite.
 */
interface MapConfigurator {
    /** Returns true if this MapFragment implementation is available on this device.  */
    fun isAvailable(context: Context): Boolean

    /**
     * Displays a warning to the user that this MapFragment implementation is
     * unavailable.  This will be invoked when isSupported() is false or
     * createMapFragment(context) returns null.
     */
    fun showUnavailableMessage(context: Context)

    /** Constructs any preference widgets that are specific to this map implementation.  */
    fun createPrefs(context: Context, settings: Settings): List<Preference>

    /** Gets the set of keys for preferences that should be watched for changes.  */
    val prefKeys: Collection<String>

    /** Packs map-related preferences into a Bundle for MapFragment.applyConfig().  */
    fun buildConfig(prefs: Settings): Bundle

    /**
     * Returns true if map fragments obtained from this MapConfigurator are
     * expected to be able to render the given file as an overlay.  This
     * check determines which files appear as available Reference Layers.
     */
    fun supportsLayer(file: File): Boolean

    /** Returns a String name for a given overlay file, or null if unsupported.  */
    fun getDisplayName(file: File): String
}
