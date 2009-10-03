package org.odk.collect.android.activities;

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
import android.util.Log;
import android.widget.Toast;

import org.odk.collect.android.R;

public class GeoPointActivity extends Activity implements LocationListener {

    ProgressDialog mLocationDialog;
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("yaw", "create");

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_location));

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        createLocationDialog();



    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i("yaw", "pause");
        mLocationManager.removeUpdates(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("yaw", "resume");
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3, this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("yaw", "destroy");
        if (mLocationDialog != null && mLocationDialog.isShowing()) {
            mLocationDialog.dismiss();
        }
    }


    private void createLocationDialog() {
        // dialog displayed while fetching gps location
        mLocationDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener geopointButtonListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // on cancel, stop gps
                        finish();
                    }
                };

        // back button doesn't cancel
        mLocationDialog.setCancelable(false);
        mLocationDialog.setIndeterminate(true);
        mLocationDialog.setTitle(getString(R.string.getting_location));
        mLocationDialog.setMessage(getString(R.string.please_wait));
        mLocationDialog.setButton(getString(R.string.cancel), geopointButtonListener);
        mLocationDialog.show();

    }


    public void onLocationChanged(Location location) {
        Intent i = new Intent();
        i.putExtra("LOCATION_RESULT", location.getLatitude() + " " + location.getLongitude());
        // return the result to the parent class
        setResult(RESULT_OK, i);
        finish();
    }


    public void onProviderDisabled(String provider) {
        Log.i("yaw", "provider disabled");
        Toast
                .makeText(getBaseContext(), getString(R.string.gps_disabled_error),
                        Toast.LENGTH_SHORT).show();

    }


    public void onProviderEnabled(String provider) {
        Log.i("yaw", "provider enabled");

    }


    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.i("yaw", "location AVAILABLE");

                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.i("yaw", "location OUT_OF_SERVICE");

                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.i("yaw", "location TEMPORARILY_UNAVAILABLE");

                break;
        }
    }



}
