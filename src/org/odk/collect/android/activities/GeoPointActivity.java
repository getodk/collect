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


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_location));

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setupLocationDialog();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        // stops the GPS. Note that this will turn off the GPS if the screen
        // goes to sleep.
        mLocationManager.removeUpdates(this);

        // We're not using managed dialogs, so we have to dismiss the dialog to
        // prevent it from leaking memory.
        if (mLocationDialog != null && mLocationDialog.isShowing()) mLocationDialog.dismiss();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3, this);
        mLocationDialog.show();
    }


    /**
     * Sets up the look and actions for the progress dialog while the GPS is
     * searching.
     */
    private void setupLocationDialog() {
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
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onLocationChanged(android.location.
     * Location)
     */
    public void onLocationChanged(Location location) {
        Intent i = new Intent();
        i.putExtra("LOCATION_RESULT", location.getLatitude() + " " + location.getLongitude());
        setResult(RESULT_OK, i);
        finish();
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    public void onProviderDisabled(String provider) {
        Toast
                .makeText(getBaseContext(), getString(R.string.gps_disabled_error),
                        Toast.LENGTH_SHORT).show();
        finish();
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    public void onProviderEnabled(String provider) {
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onStatusChanged(java.lang.String,
     * int, android.os.Bundle)
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }



}
