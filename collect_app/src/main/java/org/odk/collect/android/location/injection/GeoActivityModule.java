package org.odk.collect.android.location.injection;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.injection.ActivityModule;
import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.GeoActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.location.injection.Qualifiers.Extras;
import org.odk.collect.android.location.injection.Qualifiers.HasInitialLocation;
import org.odk.collect.android.location.injection.Qualifiers.InitialLocation;
import org.odk.collect.android.location.injection.Qualifiers.IsDraggable;
import org.odk.collect.android.location.injection.Qualifiers.IsReadOnly;
import org.odk.collect.android.location.injection.Qualifiers.ValidWithinMillis;
import org.odk.collect.android.location.model.MapFunction;
import org.odk.collect.android.location.model.MapType;

import java.text.DecimalFormat;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import static org.odk.collect.android.widgets.GeoPointWidget.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.GeoPointWidget.LOCATION;
import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;

@Module(includes = ActivityModule.class)
public abstract class GeoActivityModule {
    @Binds
    abstract FragmentActivity provideFragmentActivity(GeoActivity geoActivity);

    @Provides
    @PerActivity
    static LocationClient provideLocationClient(FragmentActivity activity) {
        return LocationClients.clientForContext(activity);
    }

    @Provides
    @PerActivity
    static SupportMapFragment provideMapFragment() {
        return SupportMapFragment.newInstance();
    }

    @Provides
    @PerActivity
    @Extras
    static Bundle provideExtras(@NonNull GeoActivity geoActivity) {
        Intent intent = geoActivity.getIntent();
        if (intent == null) {
            return Bundle.EMPTY;
        }

        Bundle extras = intent.getExtras();
        return extras != null
                ? extras
                : Bundle.EMPTY;
    }

    @Provides
    @PerActivity
    @IsDraggable
    static boolean provideIsDraggable(@Extras @NonNull Bundle bundle,
                                      @IsReadOnly boolean isReadOnly) {
        return bundle.getBoolean(DRAGGABLE_ONLY, false) && !isReadOnly;
    }

    @Provides
    @PerActivity
    @IsReadOnly
    static boolean isReadOnly(@Extras @NonNull Bundle bundle) {
        return bundle.getBoolean(READ_ONLY, false);
    }

    @Provides
    @PerActivity
    @InitialLocation
    @Nullable
    static LatLng initialLocation(@Extras @NonNull Bundle bundle) {
        double[] location = bundle.getDoubleArray(LOCATION);
        return location != null
                ? new LatLng(location[0], location[1])
                : new LatLng(0,0);
    }

    @Provides
    @PerActivity
    @HasInitialLocation
    static boolean hasInitialLocation(@InitialLocation @Nullable LatLng location) {
        return location != null;
    }

    @Provides
    @PerActivity
    static MapType provideMapType(@Extras @NonNull Bundle bundle) {
        MapType mapType = (MapType) bundle.get(GeoActivity.MAP_TYPE);
        return mapType != null
                ? mapType
                : MapType.GOOGLE;
    }

    @Provides
    @PerActivity
    static MapFunction provideMapFunction(@Extras @NonNull Bundle bundle) {
        MapFunction mapFunction = (MapFunction) bundle.get(GeoActivity.MAP_FUNCTION);
        return mapFunction != null
                ? mapFunction
                : MapFunction.POINT;
    }

    @Provides
    @PerActivity
    @ValidWithinMillis
    static double provideValidWithinMillis() {
        return 5000;
    }

    @Provides
    @PerActivity
    static DecimalFormat provideDecimalFormat() {
        return new DecimalFormat("#.##");
    }
}
