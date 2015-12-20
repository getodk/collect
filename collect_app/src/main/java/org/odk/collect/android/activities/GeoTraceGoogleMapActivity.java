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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.osmdroid.DefaultResourceProxyImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Version of the GeoTraceMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
 *
 */
public class GeoTraceGoogleMapActivity extends FragmentActivity implements LocationListener, OnMarkerDragListener, OnMapLongClickListener {
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture schedulerHandler;
	private ImageButton play_button;
	private ImageButton save_button;
	public ImageButton polygon_button;
	public ImageButton layers_button;
	public ImageButton clear_button;
	private Button manual_button;
	private ImageButton pause_button;
	private ProgressDialog progress;
	public AlertDialog.Builder builder;
	private View traceSettingsView;
	public LayoutInflater inflater;
	private AlertDialog alert;
	public AlertDialog.Builder p_builder;
	private View polygonPolylineView;
	private AlertDialog p_alert;
	private Boolean beenPaused = false;
	private Integer TRACE_MODE = 1; // 0 manual, 1 is automatic
	private Boolean play_check = false;
	private Spinner time_units;
	private Spinner time_delay;
	private SharedPreferences sharedPreferences;
	public DefaultResourceProxyImpl resource_proxy;
	private Boolean inital_location_found = false;

	private GoogleMap mMap;
	private UiSettings gmapSettings;
	private LocationManager mLocationManager;
	private Boolean mGPSOn = false;
	private Boolean mNetworkOn =false;
	private Location curLocation;
	private LatLng curlatLng;
	private Boolean initZoom = false;
	private String basemap;
	private PolylineOptions polylineOptions;
	private Polyline polygon;
	private ArrayList<LatLng> latLngsArray = new ArrayList<LatLng>();
	private ArrayList<Marker> markerArray = new ArrayList<Marker>();

	private static final String GOOGLE_MAP_STREETS = "streets";
	private static final String GOOGLE_MAP_SATELLITE = "satellite";
	private static final String GOOGLE_MAP_TERRAIN = "terrain‎";
	private static final String GOOGLE_MAP_HYBRID = "hybrid";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geotrace_google_layout);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap)).getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapLongClickListener(this);
		mMap.setOnMarkerDragListener(this);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.getUiSettings().setCompassEnabled(false);

		List<String> providers = mLocationManager.getProviders(true);
		for (String provider : providers) {
			if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
				mGPSOn = true;
				curLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
				mNetworkOn = true;
				curLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		}

		// If there is a last know location go there
		if(curLocation !=null){
			curlatLng = new LatLng(curLocation.getLatitude(),curLocation.getLongitude());
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng, 16));
			initZoom = true;
		}


		inflater = this.getLayoutInflater();
		traceSettingsView = inflater.inflate(R.layout.geotrace_dialog, null);
		polygonPolylineView = inflater.inflate(R.layout.polygon_polyline_dialog, null);
		resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		time_delay = (Spinner) traceSettingsView.findViewById(R.id.trace_delay);
		time_delay.setSelection(3);
		time_units = (Spinner) traceSettingsView.findViewById(R.id.trace_scale);
		pause_button =(ImageButton)findViewById(R.id.geotrace_pause_button);
		layers_button = (ImageButton) findViewById(R.id.geoTrace_layers_button);
		clear_button= (ImageButton) findViewById(R.id.geotrace_clear_button);
		save_button= (ImageButton) findViewById(R.id.geotrace_save);
		play_button = (ImageButton)findViewById(R.id.geotrace_play_button);
		play_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (!play_check){
					if (curLocation == null){
//						mMyLocationOverlay.runOnFirstFix(centerAroundFix);
						progress.show();

					}else{
						if(!beenPaused){
							alert.show();
						}else{
							RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(R.id.radio_group);
							int radioButtonID = rb.getCheckedRadioButtonId();
							View radioButton = rb.findViewById(radioButtonID);
							int idx = rb.indexOfChild(radioButton);
							TRACE_MODE = idx;
							if (TRACE_MODE ==0){
								setupManualMode();
							}else if (TRACE_MODE ==1){
								setupAutomaticMode();
							}else{
								reset_trace_settings();
							}
						}
						play_check=true;
					}
				}else{
					play_check=false;
					startGeoTrace();
				}
			}
		});
		progress = new ProgressDialog(this);
		progress.setTitle(getString(R.string.getting_location));
		progress.setMessage(getString(R.string.please_wait_long));
		progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				play_button.setImageResource(R.drawable.ic_menu_mylocation);
			}
		});

		manual_button = (Button)findViewById(R.id.manual_button);
		manual_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addLocationMarker();

			}
		});

		// Build ui of the dialog up front
		buildDialogs();



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
		setBasemap();

	}

	private void buildDialogs(){

		builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.geotrace_instruction));
		builder.setMessage(getString(R.string.geotrace_instruction_message));
		builder.setView(null);
		builder.setView(traceSettingsView)
				// Add action buttons
				.setPositiveButton(getString(R.string.start), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						startGeoTrace();
						dialog.cancel();
						alert.dismiss();
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						alert.dismiss();
						reset_trace_settings();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						reset_trace_settings();
					}
				});


		alert = builder.create();



		p_builder = new AlertDialog.Builder(this);
		p_builder.setTitle(getString(R.string.polygon_or_polyline));
//		p_builder.setMessage(getString(R.string.polygon_conection_message));
		p_builder.setView(polygonPolylineView)
				// Add action buttons
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();

					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.cancel();
						alert.dismiss();
					}
				});

		p_alert = p_builder.create();



	}
	private void startGeoTrace(){
		RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(R.id.radio_group);
		int radioButtonID = rb.getCheckedRadioButtonId();
		View radioButton = rb.findViewById(radioButtonID);
		int idx = rb.indexOfChild(radioButton);
		beenPaused = true;
		TRACE_MODE = idx;
		if (TRACE_MODE ==0){
			setupManualMode();
		}else if (TRACE_MODE ==1){
			setupAutomaticMode();
		}else{
			reset_trace_settings();
		}
		play_button.setVisibility(View.GONE);
		save_button.setVisibility(View.GONE);
		pause_button.setVisibility(View.VISIBLE);


	}
	private void reset_trace_settings(){
		play_button.setImageResource(R.drawable.play_button);
		play_check=false;
	}
	private void setupManualMode(){
		manual_button.setVisibility(View.VISIBLE);

	}
	private void setupAutomaticMode(){
		manual_button.setVisibility(View.VISIBLE);
		String delay = time_delay.getSelectedItem().toString();
		String units = time_units.getSelectedItem().toString();
		Long time_delay;
		TimeUnit time_units_value;
		if (units == getString(R.string.minutes)){
			time_delay = Long.parseLong(delay) * (60*60);
			time_units_value = TimeUnit.SECONDS;

		}else{
			//in Seconds
			time_delay = Long.parseLong(delay);
			time_units_value = TimeUnit.SECONDS;
		}

		setGeoTraceScheuler(time_delay, time_units_value);
	}

	public void setGeoTraceMode(View view){
		boolean checked = ((RadioButton) view).isChecked();
		switch(view.getId()) {
			case R.id.trace_manual:
				if (checked){
					TRACE_MODE = 0;
					time_units.setVisibility(View.GONE);
					time_delay.setVisibility(View.GONE);
					time_delay.invalidate();
					time_units.invalidate();
				}
				break;
			case R.id.trace_automatic:
				if (checked){
					TRACE_MODE = 1;
					time_units.setVisibility(View.VISIBLE);
					time_delay.setVisibility(View.VISIBLE);
					time_delay.invalidate();
					time_units.invalidate();
				}
				break;
		}
	}
	// The should be added to the MapHelper Class to be reused
	public void setBasemap(){
		basemap = sharedPreferences.getString(PreferencesActivity.KEY_MAP_BASEMAP, GOOGLE_MAP_STREETS);
		if (basemap.equals(GOOGLE_MAP_STREETS)) {
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}else if (basemap.equals(GOOGLE_MAP_SATELLITE)){
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		}else if(basemap.equals(GOOGLE_MAP_TERRAIN)){
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		}else if(basemap.equals(GOOGLE_MAP_HYBRID)){
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		}else{
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
	}

	/*
		This functions handels the delay and the Runable for
	*/

	public void setGeoTraceScheuler(long delay, TimeUnit units){
		schedulerHandler = scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						addLocationMarker();
					}
				});
			}
		},delay, delay, units);

	}

	private void addLocationMarker(){
		LatLng latLng = new LatLng(curLocation.getLatitude(),curLocation.getLongitude());
		//curLocation.getAccuracy();
		//curLocation.getAltitude();
		//Figure out how to retain the Accuracy for Google Map Marker
		MarkerOptions mMarkerOptions = new MarkerOptions().position(latLng).draggable(false);
		Marker marker= mMap.addMarker(mMarkerOptions);
		markerArray.add(marker);

	}


	@Override
	public void onLocationChanged(Location location) {
		if (progress.isShowing()){
			progress.dismiss();
		}
		curLocation = location;

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

	@Override
	public void onMapLongClick(LatLng latLng) {

	}

	@Override
	public void onMarkerDragStart(Marker marker) {

	}

	@Override
	public void onMarkerDrag(Marker marker) {

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {

	}
}
