package org.odk.collect.android.injection;

import android.app.Activity;

import org.odk.collect.android.activities.GoogleDriveActivity;
import org.odk.collect.android.activities.GoogleSheetsUploaderActivity;
import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.location.GeoActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Module for binding injectable Activities.
 * <p>
 * To make your Activity injectable, copy the GeoActivity binding below to match your Activity.
 * <p>
 * If you don't want to override InjectableActivity, make sure you call
 * {@link dagger.android.AndroidInjection#inject(Activity)} in your Activity's onCreate.
 *
 * @see Activity (PMD doesn't see Activity in the line above).
 */
@Module
public abstract class ActivityBuilder {

    @PerActivity
    abstract GeoActivity bindGeoActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GoogleDriveActivity bindGoogleDriveActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GoogleSheetsUploaderActivity bindGoogleSheetsUploaderActivity();
}
