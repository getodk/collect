/*
 * Copyright (C) 2009 University of Washington
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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;

public class GeoPointActivity extends Activity implements LocationListener {

	private static final String LOCATION_COUNT = "locationCount";

    private ProgressDialog mLocationDialog;
    private LocationManager mLocationManager;
    private Location mLocation;
    private boolean mGPSOn = false;
    private boolean mNetworkOn = false;
    private double mLocationAccuracy;
    private int mLocationCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null ) {
        	mLocationCount = savedInstanceState.getInt(LOCATION_COUNT);
        }

        Intent intent = getIntent();

        mLocationAccuracy = GeoPointWidget.DEFAULT_LOCATION_ACCURACY;
        if (intent != null && intent.getExtras() != null) {
        	if ( intent.hasExtra(GeoPointWidget.ACCURACY_THRESHOLD) ) {
        		mLocationAccuracy = intent.getDoubleExtra(GeoPointWidget.ACCURACY_THRESHOLD, GeoPointWidget.DEFAULT_LOCATION_ACCURACY);
        	}
        }

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_location));

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
            	InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis() +
          			   " lastKnownLocation(GPS) lat: " +
          			loc.getLatitude() + " long: " +
          			loc.getLongitude() + " acc: " +
          			loc.getAccuracy() );
        	} else {
            	InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis() +
           			   " lastKnownLocation(GPS) null location");
        	}
        }

        if ( mNetworkOn ) {
        	Location loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        	if ( loc != null ) {
            	InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis() +
          			   " lastKnownLocation(Network) lat: " +
          			loc.getLatitude() + " long: " +
          			loc.getLongitude() + " acc: " +
          			loc.getAccuracy() );
        	} else {
            	InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis() +
           			   " lastKnownLocation(Network) null location");
        	}
        }

        setupLocationDialog();

    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(LOCATION_COUNT, mLocationCount);
	}

    @Override
    protected void onPause() {
        super.onPause();

        // stops the GPS. Note that this will turn off the GPS if the screen goes to sleep.
        mLocationManager.removeUpdates(this);

        // We're not using managed dialogs, so we have to dismiss the dialog to prevent it from
        // leaking memory.
        if (mLocationDialog != null && mLocationDialog.isShowing())
            mLocationDialog.dismiss();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mGPSOn) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        if (mNetworkOn) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
        mLocationDialog.show();
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

    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    private void setupLocationDialog() {
    	Collect.getInstance().getActivityLogger().logInstanceAction(this, "setupLocationDialog", "show");
        // dialog displayed while fetching gps location
        mLocationDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener geopointButtonListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Collect.getInstance().getActivityLogger().logInstanceAction(this, "acceptLocation", "OK");
                            returnLocation();
                            break;
                        case DialogInterface. BUTTON_NEGATIVE:
                            Collect.getInstance().getActivityLogger().logInstanceAction(this, "cancelLocation", "cancel");
                            mLocation = null;
                            finish();
                            break;
                    }
                }
            };

        // back button doesn't cancel
        mLocationDialog.setCancelable(false);
        mLocationDialog.setIndeterminate(true);
        mLocationDialog.setIcon(android.R.drawable.ic_dialog_info);
        mLocationDialog.setTitle(getString(R.string.getting_location));
        mLocationDialog.setMessage(getString(R.string.please_wait_long));
        mLocationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.accept_location),
            geopointButtonListener);
        mLocationDialog.setButton(DialogInterface. BUTTON_NEGATIVE, getString(R.string.cancel_location),
            geopointButtonListener);
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


    @Override
    public void onLocationChanged(Location location) {
    	mLocation = location;
        if (mLocation != null) {
        	// Bug report: cached GeoPoint is being returned as the first value.
        	// Wait for the 2nd value to be returned, which is hopefully not cached?
        	++mLocationCount;
        	InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis() +
     			   " onLocationChanged(" + mLocationCount + ") lat: " +
         			mLocation.getLatitude() + " long: " +
         			mLocation.getLongitude() + " acc: " +
         			mLocation.getAccuracy() );

        	if (mLocationCount > 1) {
	            mLocationDialog.setMessage(getString(R.string.location_provider_accuracy,
	                mLocation.getProvider(), truncateDouble(mLocation.getAccuracy())));

	            if (mLocation.getAccuracy() <= mLocationAccuracy) {
	                returnLocation();
	            }
        	}
        } else {
        	InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis() +
      			   " onLocationChanged(" + mLocationCount + ") null location");
        }
    }


    private String truncateDouble(float number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }


    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                if (mLocation != null) {
                    mLocationDialog.setMessage(getString(R.string.location_accuracy,
                        mLocation.getAccuracy()));
                }
                break;
            case LocationProvider.OUT_OF_SERVICE:
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
        }
    }

}
