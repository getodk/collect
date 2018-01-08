package org.odk.collect.android.location.injection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.injection.config.scopes.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class GeoActivityModule {

    @NonNull
    @Provides
    @PerActivity
    FragmentManager provideFragmentManager(@NonNull AppCompatActivity activity) {
        return activity.getSupportFragmentManager();
    }

    @NonNull
    @Provides
    @PerActivity
    SupportMapFragment provideMapFragment() {
        return SupportMapFragment.newInstance();
    }

    @NonNull
    @Provides
    Bundle provideExtras(@NonNull Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return Bundle.EMPTY;
        }

        Bundle extras = intent.getExtras();

        return extras != null
                ? extras
                : Bundle.EMPTY;
    }
}
