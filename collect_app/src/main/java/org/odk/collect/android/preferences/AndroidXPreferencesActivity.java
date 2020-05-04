package org.odk.collect.android.preferences;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;

public class AndroidXPreferencesActivity extends CollectAbstractActivity {

    private static final String KEY_EXTRA_FRAGMENT = "key_extra_fragment";
    private static final int EXTRA_FRAGMENT_FORM_METADATA = 1;
    private static final int EXTRA_FRAGMENT_USER_INTERFACE = 2;

    public static <T extends PreferenceFragmentCompat> void start(Activity activity, Class<T> fragmentClass) {
        Intent intent = new Intent(activity, AndroidXPreferencesActivity.class);

        if (fragmentClass.isAssignableFrom(FormMetadataFragment.class)) {
            intent.putExtra(KEY_EXTRA_FRAGMENT, EXTRA_FRAGMENT_FORM_METADATA);
        } else if (fragmentClass.isAssignableFrom(UserInterfacePreferencesFragment.class)) {
            intent.putExtra(KEY_EXTRA_FRAGMENT, EXTRA_FRAGMENT_USER_INTERFACE);
        }

        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_layout);

        int fragmentToShow = getIntent().getExtras().getInt(KEY_EXTRA_FRAGMENT);
        Fragment fragment;

        switch (fragmentToShow) {
            case EXTRA_FRAGMENT_FORM_METADATA:
                fragment = new FormMetadataFragment();
                break;

            case EXTRA_FRAGMENT_USER_INTERFACE:
                fragment = new UserInterfacePreferencesFragment();
                break;
            default:
                fragment = null;
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.preferences_fragment_container, fragment)
                    .commit();
        }
    }
}
