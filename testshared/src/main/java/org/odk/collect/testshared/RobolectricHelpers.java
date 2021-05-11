package org.odk.collect.testshared;

import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowMediaMetadataRetriever;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import java.util.List;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;

public class RobolectricHelpers {

    private RobolectricHelpers() {
    }

    public static <T extends FragmentActivity> T createThemedActivity(Class<T> clazz, int theme) {
        ActivityController<T> activity = Robolectric.buildActivity(clazz);
        activity.get().setTheme(theme);

        return activity.setup().get();
    }

    public static int getCreatedFromResId(ImageButton button) {
        return shadowOf(button.getDrawable()).getCreatedFromResId();
    }

    public static int getCreatedFromResId(Drawable drawable) {
        return shadowOf(drawable).getCreatedFromResId();
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

    public static void mountExternalStorage() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    }

    public static <T extends ViewGroup> T populateRecyclerView(T view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);

            if (child instanceof RecyclerView) {
                child.measure(0, 0);
                child.layout(0, 0, 100, 10000);
                break;
            } else if (child instanceof ViewGroup) {
                populateRecyclerView((ViewGroup) child);
            }
        }

        return view;
    }

    @Nullable
    public static <F extends Fragment> F getFragmentByClass(FragmentManager fragmentManager, Class<F> fragmentClass) {
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.getClass().isAssignableFrom(fragmentClass)) {
                return (F) fragment;
            }
        }

        return null;
    }

    public static <F extends Fragment> FragmentScenario<F> launchDialogFragment(Class<F> fragmentClass, int theme) {
        return launchDialogFragment(fragmentClass, null, theme);
    }

    public static <F extends Fragment> FragmentScenario<F> launchDialogFragment(Class<F> fragmentClass, Bundle fragmentArgs, int theme) {
        /*
          Needed to avoid explosion (NullPointerException) inside internal platform code (WindowDecorActionBar).
          For some reason AppCompat.Light or AppCompat.Light.NoActionBar don't work. Our theme must declare
          something that is missing in those base themes when they are used in Robolectric.

          This is probably something that should be fixed within Robolectric.
         */
        ApplicationProvider.getApplicationContext().setTheme(theme);
        return FragmentScenario.launch(fragmentClass, fragmentArgs);
    }

    public static <F extends Fragment> FragmentScenario<F> launchDialogFragmentInContainer(Class<F> fragmentClass, int theme) {
        return launchDialogFragmentInContainer(fragmentClass, null, theme);
    }

    public static <F extends Fragment> FragmentScenario<F> launchDialogFragmentInContainer(Class<F> fragmentClass, Bundle fragmentArgs, int theme) {
        /*
          Needed to avoid explosion (NullPointerException) inside internal platform code (WindowDecorActionBar).
          For some reason AppCompat.Light or AppCompat.Light.NoActionBar don't work. Our theme must declare
          something that is missing in those base themes when they are used in Robolectric.

          This is probably something that should be fixed within Robolectric.
         */
        ApplicationProvider.getApplicationContext().setTheme(theme);
        return FragmentScenario.launchInContainer(fragmentClass, fragmentArgs);
    }

    public static void runLooper() {
        shadowOf(getMainLooper()).idle();
    }
}
