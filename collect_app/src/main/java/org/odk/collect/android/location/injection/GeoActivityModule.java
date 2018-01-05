package org.odk.collect.android.location.injection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.widgets.GeoPointWidget;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class GeoActivityModule {

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
