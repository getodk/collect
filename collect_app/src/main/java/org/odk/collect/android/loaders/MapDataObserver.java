package org.odk.collect.android.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MapDataObserver extends BroadcastReceiver {

	  private MapDataLoader mLoader;

public MapDataObserver(MapDataLoader loader) {
    mLoader = loader;
	
    LocalBroadcastManager.getInstance(mLoader.getContext()).registerReceiver(this,
  	      new IntentFilter("org.smap.smapTask.refresh"));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
	  Log.i("SmapPointObserver: ", "++++++++Map Observer - received refresh");
	  mLoader.onContentChanged();
  }
}
