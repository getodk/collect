package org.odk.collect.android.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;

import java.io.File;
import java.util.Collection;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Instances of this interface represent "Source" options available in the
 * "Base Layer" preferences.  Each BaseLayerSource can define its own
 * preferences that customize the base layer, and its primary job is to
 * construct a MapFragment configured according to those preferences.
 * For example, the GoogleBaseLayerSource can define a "Google map style"
 * preference with choices such as Terrain or Satellite.
 */
public interface BaseLayerSource {
    /** Returns true if this source is available on this platform and device. */
    boolean isAvailable(Context context);

    /**
     * Displays a warning to the user that this source is unavailable.  This
     * will be invoked when isSupported() is false or createMapFragment(context)
     * returns null.
     */
    void showUnavailableMessage(Context context);

    /**
     * Creates an unconfigured map fragment.  This method can return null to
     * indicate that there is no suitable MapFragment implementation available.
     */
    @Nullable MapFragment createMapFragment(Context context);

    /** Constructs any preferences that are specific to this base layer source. */
    List<Preference> createPrefs(Context context);

    /** Gets the set of keys for preferences that should be watched for changes. */
    Collection<String> getPrefKeys();

    /** Packs map-related preferences into a Bundle for MapFragment.applyConfig(). */
    Bundle buildConfig(SharedPreferences prefs);

    /**
     * Returns true if map fragments obtained from this BaseLayerSource are
     * expected to be able to render the given file as an overlay.  This
     * check determines which files appear as available Reference Layers.
     */
    boolean supportsLayer(File file);

    /** Returns a String name for a given overlay file, or null if unsupported. */
    String getDisplayName(File file);
}
