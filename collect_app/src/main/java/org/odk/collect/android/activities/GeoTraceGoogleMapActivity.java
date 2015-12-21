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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.widgets.GeoTraceWidget;
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
	private Boolean clear_button_test = false;


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
	private Polyline polyline;
	private String final_return_string;
	private ArrayList<LatLng> latLngsArray = new ArrayList<LatLng>();
	private ArrayList<Marker> markerArray = new ArrayList<Marker>();
	private Button polygon_save;
	private Button polyline_save;

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

		polylineOptions = new PolylineOptions();
		polylineOptions.color(Color.RED);

		progress = new ProgressDialog(this);
		progress.setTitle(getString(R.string.getting_location));
		progress.setMessage(getString(R.string.please_wait_long));
		progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				play_button.setImageResource(R.drawable.ic_menu_mylocation);
			}
		});


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


		if (mGPSOn) {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GeoTraceGoogleMapActivity.this);
		}
		if (mNetworkOn) {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, GeoTraceGoogleMapActivity.this);
		}


		inflater = this.getLayoutInflater();
		traceSettingsView = inflater.inflate(R.layout.geotrace_dialog, null);
		polygonPolylineView = inflater.inflate(R.layout.polygon_polyline_dialog, null);
		resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		time_delay = (Spinner) traceSettingsView.findViewById(R.id.trace_delay);
		time_delay.setSelection(3);
		time_units = (Spinner) traceSettingsView.findViewById(R.id.trace_scale);
		pause_button =(ImageButton)findViewById(R.id.geotrace_pause_button);
		pause_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				play_button.setVisibility(View.VISIBLE);
				save_button.setVisibility(View.VISIBLE);
				pause_button.setVisibility(View.GONE);
				manual_button.setVisibility(View.GONE);
				play_check = true;
				try {
					schedulerHandler.cancel(true);
				} catch (Exception e) {
					// Do nothing
				}


			}
		});
		layers_button = (ImageButton) findViewById(R.id.geoTrace_layers_button);
		clear_button= (ImageButton) findViewById(R.id.geotrace_clear_button);
		save_button= (ImageButton) findViewById(R.id.geotrace_save);
		save_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				p_alert.show();

			}
		});
		play_button = (ImageButton)findViewById(R.id.geotrace_play_button);
		play_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (!play_check) {
					if (curLocation == null) {
						progress.show();
					} else {
						if (!beenPaused) {
							alert.show();
						} else {
							RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(R.id.radio_group);
							int radioButtonID = rb.getCheckedRadioButtonId();
							View radioButton = rb.findViewById(radioButtonID);
							int idx = rb.indexOfChild(radioButton);
							TRACE_MODE = idx;
							if (TRACE_MODE == 0) {
								setupManualMode();
							} else if (TRACE_MODE == 1) {
								setupAutomaticMode();
							} else {
								reset_trace_settings();
							}
						}
						play_check = true;
					}
				} else {
					play_check = false;
					startGeoTrace();
				}
			}
		});

		manual_button = (Button)findViewById(R.id.manual_button);
		manual_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addLocationMarker();

			}
		});

		polygon_save = (Button) polygonPolylineView.findViewById(R.id.polygon_save);
		polygon_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (markerArray.size() > 2) {
					createPolygon();
					p_alert.dismiss();
					saveGeoTrace();
				} else {
					p_alert.dismiss();
					showPolyonErrorDialog();
				}


			}
		});
		polyline_save = (Button) polygonPolylineView.findViewById(R.id.polyline_save);
		polyline_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				p_alert.dismiss();
				saveGeoTrace();

			}
		});

		// Build ui of the dialog up front
		buildDialogs();
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			if ( intent.hasExtra(GeoTraceWidget.TRACE_LOCATION) ) {
				String s = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
				play_button.setVisibility(View.GONE);
				clear_button.setVisibility(View.VISIBLE);
				overlayIntentTrace(s);
				zoomToCentroid();
			}
		}else{
			// If there is a last know location go there
			if(curLocation !=null){
				curlatLng = new LatLng(curLocation.getLatitude(),curLocation.getLongitude());
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng, 16));
				initZoom = true;
				progress.hide();
			}else{
				// There is no progress for searching
				progress.show();
			}

		}



	}

	private void overlayIntentTrace(String str){
		mMap.setOnMapLongClickListener(null);
		clear_button.setVisibility(View.VISIBLE);
		clear_button_test = true;
		String s = str.replace("; ",";");
		String[] sa = s.split(";");
		for (int i=0;i<(sa.length -1 );i++){
			String[] sp = sa[i].split(" ");
			double gp[] = new double[4];
			String lat = sp[0].replace(" ", "");
			String lng = sp[1].replace(" ", "");
			gp[0] = Double.parseDouble(lat);
			gp[1] = Double.parseDouble(lng);
			LatLng point = new LatLng(gp[0], gp[1]);
			polylineOptions.add(point);
			MarkerOptions mMarkerOptions = new MarkerOptions().position(point).draggable(true);
			Marker marker= mMap.addMarker(mMarkerOptions);
			markerArray.add(marker);
		}
		polyline = mMap.addPolyline(polylineOptions);
		update_polyline();

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

		final_return_string = generateReturnString();
		Intent i = new Intent();
		i.putExtra(
				FormEntryActivity.GEOTRACE_RESULTS,
				final_return_string);
		setResult(RESULT_OK, i);
		finish();
	}

	private String generateReturnString() {
		String temp_string = "";
		for (int i = 0 ; i < markerArray.size();i++){
			String lat = Double.toString(markerArray.get(i).getPosition().latitude);
			String lng = Double.toString(markerArray.get(i).getPosition().longitude);
			String alt ="0.0" ; // Figure out how to get the alt;
			String acu = "0.0"; // Figure out how to get the acu;
			temp_string = temp_string+lat+" "+lng +" "+alt+" "+acu+";";
		}
		return temp_string;
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
	private void createPolygon(){
		markerArray.add(markerArray.get(0));
		update_polyline();
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

	private void update_polyline(){
		ArrayList<LatLng> tempLat =  new ArrayList<LatLng>();
		for (int i =0;i<markerArray.size();i++){
			LatLng latLng = markerArray.get(i).getPosition();
			tempLat.add(latLng);
		}
		latLngsArray = tempLat;
		polyline.setPoints(tempLat);
	}

	private void addLocationMarker(){
		LatLng latLng = new LatLng(curLocation.getLatitude(),curLocation.getLongitude());
		//curLocation.getAccuracy();
		//curLocation.getAltitude();
		//Figure out how to retain the Accuracy for Google Map Marker
		MarkerOptions mMarkerOptions = new MarkerOptions().position(latLng).draggable(true);
		Marker marker= mMap.addMarker(mMarkerOptions);
		markerArray.add(marker);
		if (polyline == null){
			polylineOptions.add(latLng);
			polyline = mMap.addPolyline(polylineOptions);
		}else{
			update_polyline();
		}


	}

	private void saveGeoTrace(){
		returnLocation();
		finish();
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
		update_polyline();

	}

	@Override
	public void onMarkerDrag(Marker marker) {
		update_polyline();
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		update_polyline();
	}

	private void showPolyonErrorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.polygon_validator))
				.setPositiveButton(getString(R.string.dialog_continue), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// FIRE ZE MISSILES!
					}
				}).show();

	}


	private void zoomToCentroid(){

        /*
            Calculate Centroid of Polyline
         */

		Handler handler=new Handler();
		Runnable r = new Runnable(){
			public void run() {
				Integer size  = markerArray.size();
				Double x_value = 0.0;
				Double y_value = 0.0;
				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				for(int i=0; i<size; i++){
					LatLng temp_marker = markerArray.get(i).getPosition();
					Double x_marker = temp_marker.latitude;
					Double y_marker = temp_marker.longitude;
					LatLng ll = new LatLng(x_marker,y_marker);
					builder.include(ll);
					x_value += x_marker;
					y_value += y_marker;
				}

				Double x_cord = x_value/size;
				Double y_cord = y_value/size;
				LatLng centroid = new LatLng(x_cord,y_cord);
				LatLngBounds bounds = builder.build();
				int width = getResources().getDisplayMetrics().widthPixels;
				int height = getResources().getDisplayMetrics().heightPixels;

				mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,width, height,20));

			}
		};

		handler.post(r);

	}
}
