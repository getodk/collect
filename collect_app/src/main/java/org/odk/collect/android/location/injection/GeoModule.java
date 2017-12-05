package org.odk.collect.android.location.injection;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.injection.config.scopes.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class GeoModule {

    @Provides
    @PerActivity
    FragmentManager provideFragmentManager(AppCompatActivity activity) {
        return activity.getSupportFragmentManager();
    }

    @Provides
    SupportMapFragment provideMapFragment() {
        return SupportMapFragment.newInstance();
    }
}
