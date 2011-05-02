
package org.odk.collect.android.activities;

import java.text.DecimalFormat;
import java.util.List;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class GeoPointActivity extends MapActivity implements LocationListener {

    private MapView mMapView;
    private TextView mLocationStatus;

    private MapController mMapController;
    private LocationManager mLocationManager;
    private MyLocationOverlay mLocationOverlay;
    private GeoPoint mGeoPoint;
    private Location mLocation;

    private static double LOCATION_ACCURACY = 5;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.geopoint_layout);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // make sure we have at least one non-passive location provider before continuing
        List<String> providers = mLocationManager.getProviders(true);
        boolean gps = false;
        boolean network = false;
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                gps = true;
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                network = true;
            }
        }
        if (!gps && !network) {
            Toast.makeText(getBaseContext(), getString(R.string.provider_disabled_error),
                Toast.LENGTH_SHORT).show();
            finish();
        }

        mMapView = (MapView) findViewById(R.id.mapview);
        mLocationStatus = (TextView) findViewById(R.id.location_status);

        mMapView.setBuiltInZoomControls(true);
        mMapView.setSatellite(false);

        mMapController = mMapView.getController();
        mMapController.setZoom(16);

        mLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mLocationOverlay);

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
        return new DecimalFormat("#").format(f);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
        mLocationOverlay.disableMyLocation();
    }


    @Override
    protected void onResume() {
        super.onResume();

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        mLocationOverlay.enableMyLocation();
    }


    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }


    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (mLocation != null) {
            mLocationStatus.setText(getString(R.string.location_provider_accuracy,
                mLocation.getProvider(), truncateFloat(mLocation.getAccuracy())));
            mGeoPoint =
                new GeoPoint((int) (mLocation.getLatitude() * 1E6),
                        (int) (mLocation.getLongitude() * 1E6));

            mMapController.animateTo(mGeoPoint);

            if (mLocation.getAccuracy() <= LOCATION_ACCURACY) {
                returnLocation();
            }
        }
    }


    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
}
