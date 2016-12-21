package org.odk.collect.android.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.activities.MapsActivity;
import org.odk.collect.android.fragments.MapFragment;


public class MapLocationObserver extends BroadcastReceiver {

    private MapsActivity mMap = null;
    SharedPreferences settings = null;

public MapLocationObserver(Context context, MapsActivity map) {
    mMap = map;

    settings = PreferenceManager.getDefaultSharedPreferences(context);

    LocalBroadcastManager.getInstance(context).registerReceiver(this,
            new IntentFilter("locationChanged"));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
      Location locn = Collect.getInstance().getLocation();
      LatLng point = new LatLng(locn.getLatitude(), locn.getLongitude());
      if(settings.getBoolean(PreferencesActivity.KEY_STORE_USER_TRAIL, false)) {
          mMap.updatePath(point);
      }
      //mMap.setUserLocation(Collect.getInstance().getLocation(), settings.getBoolean(PreferencesActivity.KEY_STORE_USER_TRAIL, false));
  }
}
