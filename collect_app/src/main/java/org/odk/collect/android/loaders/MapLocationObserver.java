package org.odk.collect.android.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.SmapTaskMapFragment;
import org.odk.collect.android.preferences.GeneralKeys;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MapLocationObserver extends BroadcastReceiver {

    private SmapTaskMapFragment mMap = null;
    SharedPreferences sharedPreferences = null;

public MapLocationObserver(Context context, SmapTaskMapFragment map) {
    mMap = map;

    LocalBroadcastManager.getInstance(context).registerReceiver(this,
            new IntentFilter("locationChanged"));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
      Location locn = Collect.getInstance().getLocation();
      LatLng point = new LatLng(locn.getLatitude(), locn.getLongitude());
      mMap.updatePath(point);
  }
}
