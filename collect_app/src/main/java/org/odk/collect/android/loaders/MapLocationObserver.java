package org.odk.collect.android.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.activities.MapsActivity;
import org.odk.collect.android.fragments.MapFragment;


public class MapLocationObserver extends BroadcastReceiver {

    private MapFragment mMap = null;
    SharedPreferences settings = null;

public MapLocationObserver(Context context, MapFragment map) {
    mMap = map;

    settings = PreferenceManager.getDefaultSharedPreferences(context);

    LocalBroadcastManager.getInstance(context).registerReceiver(this,
            new IntentFilter("locationChanged"));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
      Log.i("Maps Activity: ", "++++++++received location change");
      //mMap.setUserLocation(Collect.getInstance().getLocation(), settings.getBoolean(PreferencesActivity.KEY_STORE_USER_TRAIL, false));
  }
}
