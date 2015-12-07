/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

/**
 * Version of the GeoPointMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author guisalmon@gmail.com
 *
 */
public class GeoTraceGoogleMapActivity extends Activity implements LocationListener{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geopoint_layout);

	}

	private void stopGeolocating() {

	}

	@Override
	protected void onStart() {
		super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this);
	}

	@Override
	protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this);
		super.onStop();
	}


	private void returnLocation() {
		finish();
	}




	@Override
	protected void onPause() {
		super.onPause();

	}


	@Override
	protected void onResume() {
		super.onResume();

	}


	@Override
	public void onLocationChanged(Location location) {
	}


	@Override
	public void onProviderDisabled(String provider) {
	}


	@Override
	public void onProviderEnabled(String provider) {
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}





}
