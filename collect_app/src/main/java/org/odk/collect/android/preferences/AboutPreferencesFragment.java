package org.odk.collect.android.preferences;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.OpenSourceLicensesActivity;

import java.util.List;


public class AboutPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final String KEY_OPEN_SOURCE_LICENSES = "open_source_licenses";
    public static final String KEY_TELL_YOUR_FRIENDS = "tell_your_friends";
    public static final String KEY_LEAVE_A_REVIEW = "leave_a_review";
    public static final String KEY_ODK_WEBSITE = "info";
    private static final String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=";
    private static final String ODK_WEBSITE = "https://opendatakit.org";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_preferences);

        PreferenceScreen mODKWebsitePreference = (PreferenceScreen) findPreference(
                KEY_ODK_WEBSITE);
        PreferenceScreen mOpenSourceLicensesPreference = (PreferenceScreen) findPreference(
                KEY_OPEN_SOURCE_LICENSES);
        PreferenceScreen mTellYourFriendsPreference = (PreferenceScreen) findPreference(
                KEY_TELL_YOUR_FRIENDS);
        PreferenceScreen mLeaveAReviewPreference = (PreferenceScreen) findPreference(
                KEY_LEAVE_A_REVIEW);

        mODKWebsitePreference.setOnPreferenceClickListener(this);
        mOpenSourceLicensesPreference.setOnPreferenceClickListener(this);
        mTellYourFriendsPreference.setOnPreferenceClickListener(this);
        mLeaveAReviewPreference.setOnPreferenceClickListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String APP_PACKAGE_NAME = getActivity().getPackageName();

        switch (preference.getKey()) {
            case KEY_ODK_WEBSITE:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ODK_WEBSITE));
                startActivity(intent);
                break;

            case KEY_OPEN_SOURCE_LICENSES:
                startActivity(new Intent(getActivity().getApplicationContext(),
                        OpenSourceLicensesActivity.class));
                break;

            case KEY_TELL_YOUR_FRIENDS:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.tell_your_friends_msg) + " " + GOOGLE_PLAY_URL +
                                APP_PACKAGE_NAME);
                startActivity(Intent.createChooser(shareIntent,
                        getString(R.string.tell_your_friends_title)));
                break;

            case KEY_LEAVE_A_REVIEW:
                boolean reviewTaken = false;
                try {
                    // Open the google play store app if present
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + APP_PACKAGE_NAME));
                    PackageManager packageManager = getActivity().getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
                    for (ResolveInfo info : list) {
                        ActivityInfo activity = info.activityInfo;
                        if (activity.name.contains("com.google.android")) {
                            ComponentName name = new ComponentName(
                                    activity.applicationInfo.packageName,
                                    activity.name);
                            intent.setComponent(name);
                            startActivity(intent);
                            reviewTaken = true;
                        }
                    }
                } catch (android.content.ActivityNotFoundException anfe) {
                    anfe.printStackTrace();
                }
                if (!reviewTaken) {
                    // Show a list of all available browsers if user doesn't have a default browser
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(GOOGLE_PLAY_URL + APP_PACKAGE_NAME));
                    startActivity(intent);
                }
                break;
        }
        return true;
    }
}