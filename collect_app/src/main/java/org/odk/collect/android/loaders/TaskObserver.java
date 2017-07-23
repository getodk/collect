package org.odk.collect.android.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import timber.log.Timber;


public class TaskObserver extends BroadcastReceiver {

	  private TaskLoader mLoader;
	  
public TaskObserver(TaskLoader loader) {
    mLoader = loader;
	
    LocalBroadcastManager.getInstance(mLoader.getContext()).registerReceiver(this,
  	      new IntentFilter("refresh"));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
	  Timber.i("++++++++received refresh");
	  mLoader.onContentChanged();
  }
}
