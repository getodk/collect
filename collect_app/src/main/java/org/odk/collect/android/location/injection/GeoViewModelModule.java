package org.odk.collect.android.location.injection;

import android.content.Context;
import android.support.annotation.NonNull;

import org.odk.collect.android.injection.config.scopes.PerViewModel;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;

import java.text.DecimalFormat;

import dagger.Module;
import dagger.Provides;

@Module
public class GeoViewModelModule {

    @NonNull
    @Provides
    @PerViewModel
    LocationClient provideLocationClient(@NonNull Context context) {
        return LocationClients.clientForContext(context);
    }

    @NonNull
    @Provides
    @PerViewModel
    DecimalFormat provideDecimalFormat() {
        return new DecimalFormat("#.##");
    }
}
