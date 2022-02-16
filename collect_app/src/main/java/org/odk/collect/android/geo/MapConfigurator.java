package org.odk.collect.android.geo;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.shared.settings.Settings;

import java.io.File;
import java.util.Collection;
import java.util.List;

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
public interface MapConfigurator {
    /** Returns true if this MapFragment implementation is available on this device. */
    boolean isAvailable(Context context);

    /**
     * Displays a warning to the user that this MapFragment implementation is
     * unavailable.  This will be invoked when isSupported() is false or
     * createMapFragment(context) returns null.
     */
    void showUnavailableMessage(Context context);

    /**
     * Creates an unconfigured map fragment (of the type configurable by this
     * MapConfigurator).  This method may return null to indicate that there is
     * no suitable MapFragment implementation available.
     */
    @Nullable
    MapFragment createMapFragment(Context context);

    /** Constructs any preference widgets that are specific to this map implementation. */
    List<Preference> createPrefs(Context context);

    /** Gets the set of keys for preferences that should be watched for changes. */
    Collection<String> getPrefKeys();

    /** Packs map-related preferences into a Bundle for MapFragment.applyConfig(). */
    Bundle buildConfig(Settings prefs);

    /**
     * Returns true if map fragments obtained from this MapConfigurator are
     * expected to be able to render the given file as an overlay.  This
     * check determines which files appear as available Reference Layers.
     */
    boolean supportsLayer(File file);

    /** Returns a String name for a given overlay file, or null if unsupported. */
    String getDisplayName(File file);
}
