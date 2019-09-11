package org.odk.collect.android.support;

import android.media.MediaMetadataRetriever;
import android.widget.ImageButton;

import androidx.fragment.app.FragmentActivity;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowMediaMetadataRetriever;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import static org.robolectric.Shadows.shadowOf;

public class RobolectricHelpers {

    private RobolectricHelpers() {}

    public static void overrideAppDependencyModule(AppDependencyModule appDependencyModule) {
        AppDependencyComponent testComponent = DaggerAppDependencyComponent.builder()
                .application(RuntimeEnvironment.application)
                .appDependencyModule(appDependencyModule)
                .build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
    }

    public static AppDependencyComponent getApplicationComponent() {
        return ((Collect) RuntimeEnvironment.application).getComponent();
    }

    public static <T extends FragmentActivity> T createThemedActivity(Class<T> clazz) {
        T activity = Robolectric.setupActivity(clazz);
        activity.setTheme(R.style.Theme_Collect_Light); // Needed so attrs are available

        return activity;
    }

    public static FragmentActivity createThemedActivity() {
        return createThemedActivity(FragmentActivity.class);
    }

    public static <T extends FragmentActivity> ActivityController<T> buildThemedActivity(Class<T> clazz) {
        ActivityController<T> activity = Robolectric.buildActivity(clazz);
        activity.get().setTheme(R.style.Theme_Collect_Light); // Needed so attrs are available

        return activity;
    }

    public static int getCreatedFromResId(ImageButton button) {
        return shadowOf(button.getDrawable()).getCreatedFromResId();
    }

    public static DataSource setupMediaPlayerDataSource(String testFile) {
        return setupMediaPlayerDataSource(testFile, 322450);
    }

    public static DataSource setupMediaPlayerDataSource(String testFile, Integer duration) {
        DataSource dataSource = DataSource.toDataSource(testFile);
        ShadowMediaMetadataRetriever.addMetadata(dataSource, MediaMetadataRetriever.METADATA_KEY_DURATION, duration.toString());
        ShadowMediaPlayer.addMediaInfo(dataSource, new ShadowMediaPlayer.MediaInfo(duration, 0));
        return dataSource;
    }
}
