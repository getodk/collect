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

import java.text.DecimalFormat;
import java.util.List;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.InfoLogger;
import org.odk.collect.android.widgets.GeoPointWidget;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class GeoPointMapActivitySdk7 extends MapActivity implements LocationListener {

	private static final String LOCATION_COUNT = "locationCount";

    private MapView mMapView;
    private TextView mLocationStatus;

    private MapController mMapController;
    private LocationManager mLocationManager;
    private Overlay mLocationOverlay;
    private Overlay mGeoPointOverlay;

    private GeoPoint mGeoPoint;
    private Location mLocation;
    private Button mAcceptLocation;
    private Button mCancelLocation;

    private boolean mCaptureLocation = true;
    private Button mShowLocation;

    private boolean mGPSOn = false;
    private boolean mNetworkOn = false;

    private double mLocationAccuracy;
    private int mLocationCount = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null ) {
        	mLocationCount = savedInstanceState.getInt(LOCATION_COUNT);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.geopoint_layout_sdk7);

        Intent intent = getIntent();

        mLocationAccuracy = GeoPointWidget.DEFAULT_LOCATION_ACCURACY;
        if (intent != null && intent.getExtras() != null) {
        	if ( intent.hasExtra(GeoPointWidget.LOCATION) ) {
        		double[] location = intent.getDoubleArrayExtra(GeoPointWidget.LOCATION);
            	mGeoPoint = new GeoPoint((int) (location[0] * 1E6), (int) (location[1] * 1E6));
            }
        	if ( intent.hasExtra(GeoPointWidget.ACCURACY_THRESHOLD) ) {
        		mLocationAccuracy = intent.getDoubleExtra(GeoPointWidget.ACCURACY_THRESHOLD, GeoPointWidget.DEFAULT_LOCATION_ACCURACY);
        	}
    		mCaptureLocation = !intent.getBooleanExtra(GeoPointWidget.READ_ONLY, false);
        }

        /**
         * Add the MapView dynamically to the placeholding frame so as to not
         * incur the wrath of Android Lint...
         */
        FrameLayout frame = (FrameLayout) findViewById(R.id.mapview_placeholder);
        String apiKey = "017Xo9E6R7WmcCITvo-lU2V0ERblKPqCcguwxSQ";
        // String apiKey = "0wsgFhRvVBLVpgaFzmwaYuqfU898z_2YtlKSlkg";
        mMapView = new MapView(this, apiKey);
        mMapView.setClickable(true);
        mMapView.setId(R.id.mapview);
        LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        frame.addView(mMapView, p);

        mCancelLocation = (Button) findViewById(R.id.cancel_location);
        mCancelLocation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "cancelLocation", "cancel");
                finish();
            }
        });

        mMapController = mMapView.getController();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mMapView.setBuiltInZoomControls(true);
        mMapView.setSatellite(false);
        mMapController.setZoom(16);

        // make sure we have a good location provider before continuing
        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                mGPSOn = true;
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                mNetworkOn = true;
            }
        }
        if (!mGPSOn && !mNetworkOn) {
            Toast.makeText(getBaseContext(), getString(R.string.provider_disabled_error),
                Toast.LENGTH_SHORT).show();
            finish();
        }

        if ( mGPSOn ) {
        	Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        	if ( loc != null ) {
            	InfoLogger.geolog("GeoPointMapActivity: " + System.currentTimeMillis() +
          			   " lastKnownLocation(GPS) lat: " +
          			loc.getLatitude() + " long: " +
          			loc.getLongitude() + " acc: " +
          			loc.getAccuracy() );
        	} else {
            	InfoLogger.geolog("GeoPointMapActivity: " + System.currentTimeMillis() +
           			   " lastKnownLocation(GPS) null location");
        	}
        }

        if ( mNetworkOn ) {
        	Location loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        	if ( loc != null ) {
            	InfoLogger.geolog("GeoPointMapActivity: " + System.currentTimeMillis() +
          			   " lastKnownLocation(Network) lat: " +
          			loc.getLatitude() + " long: " +
          			loc.getLongitude() + " acc: " +
          			loc.getAccuracy() );
        	} else {
            	InfoLogger.geolog("GeoPointMapActivity: " + System.currentTimeMillis() +
           			   " lastKnownLocation(Network) null location");
        	}
        }

        mLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mLocationOverlay);

        if (mCaptureLocation) {
            mLocationStatus = (TextView) findViewById(R.id.location_status);
            mAcceptLocation = (Button) findViewById(R.id.accept_location);
            mAcceptLocation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Collect.getInstance().getActivityLogger().logInstanceAction(this, "acceptLocation", "OK");
                    returnLocation();
                }
            });

        } else {

            mGeoPointOverlay = new Marker(mGeoPoint);
            mMapView.getOverlays().add(mGeoPointOverlay);

            ((Button) findViewById(R.id.accept_location)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.location_status)).setVisibility(View.GONE);
            mShowLocation = ((Button) findViewById(R.id.show_location));
            mShowLocation.setVisibility(View.VISIBLE);
            mShowLocation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Collect.getInstance().getActivityLogger().logInstanceAction(this, "showLocation", "onClick");
                    mMapController.animateTo(mGeoPoint);
                }
            });

        }

        if ( mGeoPoint != null ) {
            mMapController.animateTo(mGeoPoint);
        }
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
        if (mLocation != null) {
            Intent i = new Intent();
            i.putExtra(
                FormEntryActivity.LOCATION_RESULT,
                mLocation.getLatitude() + " " + mLocation.getLongitude() + " "
                        + mLocation.getAltitude() + " " + mLocation.getAccuracy());
            setResult(RESULT_OK, i);
        }
        finish();
    }


    private String truncateFloat(float f) {
        return new DecimalFormat("#.##").format(f);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
        ((MyLocationOverlay) mLocationOverlay).disableMyLocation();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCaptureLocation) {
	        ((MyLocationOverlay) mLocationOverlay).enableMyLocation();
	        if (mGPSOn) {
	            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	        }
	        if (mNetworkOn) {
	            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	        }
        }
    }


    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }


    @Override
    public void onLocationChanged(Location location) {
        if (mCaptureLocation) {
            mLocation = location;
            if (mLocation != null) {
            	// Bug report: cached GeoPoint is being returned as the first value.
            	// Wait for the 2nd value to be returned, which is hopefully not cached?
            	++mLocationCount;
            	InfoLogger.geolog("GeoPointMapActivity: " + System.currentTimeMillis() +
          			   " onLocationChanged(" + mLocationCount + ") lat: " +
              			mLocation.getLatitude() + " long: " +
              			mLocation.getLongitude() + " acc: " +
              			mLocation.getAccuracy() );

            	if (mLocationCount > 1) {
            		mLocationStatus.setText(getString(R.string.location_provider_accuracy,
            				mLocation.getProvider(), truncateFloat(mLocation.getAccuracy())));
	                mGeoPoint =
	                    new GeoPoint((int) (mLocation.getLatitude() * 1E6),
	                            (int) (mLocation.getLongitude() * 1E6));

	                mMapController.animateTo(mGeoPoint);

	                if (mLocation.getAccuracy() <= mLocationAccuracy) {
	                    returnLocation();
	                }
            	}
    	    } else {
    	    	InfoLogger.geolog("GeoPointMapActivity: " + System.currentTimeMillis() +
    	  			   " onLocationChanged(" + mLocationCount + ") null location");
            }
	    }
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

    class Marker extends Overlay {
        GeoPoint gp = null;


        public Marker(GeoPoint gp) {
            super();
            this.gp = gp;
        }


        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            super.draw(canvas, mapView, shadow);
            Point screenPoint = new Point();
            mMapView.getProjection().toPixels(gp, screenPoint);
            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_maps_indicator_current_position), screenPoint.x, screenPoint.y - 8,
                null); // -8 as image is 16px high
        }
    }

}
