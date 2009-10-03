package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import org.odk.collect.android.R;

public class GeoPointActivity extends Activity implements LocationListener {

    ProgressDialog mLocationDialog;
    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_location));

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        createLocationDialog();

    }


    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3, this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        setResult(RESULT_OK, i);
        finish();
    }


    public void onProviderDisabled(String provider) {
        Toast
                .makeText(getBaseContext(), getString(R.string.gps_disabled_error),
                        Toast.LENGTH_SHORT).show();
        finish();

    }


    public void onProviderEnabled(String provider) {

    }


    public void onStatusChanged(String provider, int status, Bundle extras) {
    }



}
