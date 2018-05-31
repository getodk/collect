package org.odk.collect.android.injection;

import android.app.Activity;
import android.preference.PreferenceActivity;

import org.odk.collect.android.activities.AboutActivity;
import org.odk.collect.android.activities.BearingActivity;
import org.odk.collect.android.activities.CaptureSelfieActivity;
import org.odk.collect.android.activities.CaptureSelfieActivityNewApi;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.activities.EditFormHierarchyActivity;
import org.odk.collect.android.activities.FileManagerTabs;
import org.odk.collect.android.activities.FormChooserList;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.activities.GeoPointOsmMapActivity;
import org.odk.collect.android.activities.GeoShapeGoogleMapActivity;
import org.odk.collect.android.activities.GeoShapeOsmMapActivity;
import org.odk.collect.android.activities.GeoTraceGoogleMapActivity;
import org.odk.collect.android.activities.GeoTraceOsmMapActivity;
import org.odk.collect.android.activities.GoogleDriveActivity;
import org.odk.collect.android.activities.GoogleSheetsUploaderActivity;
import org.odk.collect.android.activities.InstanceChooserList;
import org.odk.collect.android.activities.InstanceUploaderActivity;
import org.odk.collect.android.activities.InstanceUploaderList;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.activities.ViewFormHierarchyActivity;
import org.odk.collect.android.activities.WebViewActivity;
import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.location.GeoActivity;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;

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
    abstract SplashScreenActivity splashScreenActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract MainMenuActivity mainMenuActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract FormChooserList formChooserList();

    @PerActivity
    @ContributesAndroidInjector
    abstract InstanceChooserList instanceChooserList();

    @PerActivity
    @ContributesAndroidInjector
    abstract InstanceUploaderList instanceUploaderList();

    @PerActivity
    @ContributesAndroidInjector
    abstract FormDownloadList formDownloadList();

    @PerActivity
    @ContributesAndroidInjector
    abstract GoogleDriveActivity googleDriveActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract FileManagerTabs fileManagerTabs();

    @PerActivity
    @ContributesAndroidInjector
    abstract FormEntryActivity formEntryActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract PreferenceActivity preferenceActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract AdminPreferencesActivity adminPreferencesActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract PreferencesActivity preferencesActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract AboutActivity aboutActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract WebViewActivity webViewActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract EditFormHierarchyActivity editFormHierarchyActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract InstanceUploaderActivity instanceUploaderActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract ViewFormHierarchyActivity viewFormHierarchyActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GeoPointActivity geoPointActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GeoPointMapActivity geoPointMapActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GeoPointOsmMapActivity geoPointOsmMapActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GeoShapeGoogleMapActivity geoShapeGoogleMapActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GeoShapeOsmMapActivity geoShapeOsmMapActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GeoTraceGoogleMapActivity geoTraceGoogleMapActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GeoTraceOsmMapActivity geoTraceOsmMapActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract ScannerWithFlashlightActivity scannerWithFlashlightActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract NotificationActivity notificationActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract DrawActivity drawActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract BearingActivity bearingActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract GoogleSheetsUploaderActivity googleSheetsUploaderActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract CaptureSelfieActivity captureSelfieActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract CaptureSelfieActivityNewApi captureSelfieActivityNewApi();
}
