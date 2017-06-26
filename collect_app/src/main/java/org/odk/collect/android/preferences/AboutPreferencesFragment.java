package org.odk.collect.android.preferences;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.view.View;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.OpenSourceLicensesActivity;
import org.odk.collect.android.utilities.CustomTabHelper;

import java.util.List;

import timber.log.Timber;


public class AboutPreferencesFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final String KEY_OPEN_SOURCE_LICENSES = "open_source_licenses";
    public static final String KEY_TELL_YOUR_FRIENDS = "tell_your_friends";
    public static final String KEY_LEAVE_A_REVIEW = "leave_a_review";
    public static final String KEY_ODK_WEBSITE = "odk_website";
    public static final String KEY_ODK_FORUM = "odk_forum";
    private static final String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=";
    private static final String ODK_WEBSITE = "https://opendatakit.org";
    private static final String ODK_FORUM = "https://forum.opendatakit.org";

    private CustomTabHelper websiteTabHelper;
    private CustomTabHelper forumTabHelper;

    private Uri websiteUri;
    private Uri forumUri;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_preferences);

        findPreference(KEY_ODK_WEBSITE).setOnPreferenceClickListener(this);
        findPreference(KEY_ODK_FORUM).setOnPreferenceClickListener(this);
        findPreference(KEY_OPEN_SOURCE_LICENSES).setOnPreferenceClickListener(this);
        findPreference(KEY_TELL_YOUR_FRIENDS).setOnPreferenceClickListener(this);
        findPreference(KEY_LEAVE_A_REVIEW).setOnPreferenceClickListener(this);
        websiteTabHelper = new CustomTabHelper();
        forumTabHelper = new CustomTabHelper();
        websiteUri = Uri.parse(ODK_WEBSITE);
        forumUri = Uri.parse(ODK_FORUM);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(getString(R.string.about_preferences));
    }

    @Override
    public void onStart() {
        super.onStart();
        websiteTabHelper.bindCustomTabsService(this.getActivity(), websiteUri);
        forumTabHelper.bindCustomTabsService(this.getActivity(), forumUri);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String APP_PACKAGE_NAME = getActivity().getPackageName();

        switch (preference.getKey()) {
            case KEY_ODK_WEBSITE:
                if (websiteTabHelper.getPackageName(getActivity()).size() != 0) {
                    CustomTabsIntent customTabsIntent =
                            new CustomTabsIntent.Builder()
                                    .build();
                    customTabsIntent.intent.setPackage(websiteTabHelper.getPackageName(getActivity()).get(0));
                    customTabsIntent.launchUrl(getActivity(), websiteUri);
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ODK_WEBSITE)));
                }
                break;

            case KEY_ODK_FORUM:
                if (forumTabHelper.getPackageName(getActivity()).size() != 0) {
                    CustomTabsIntent customTabsIntent =
                            new CustomTabsIntent.Builder()
                                    .build();
                    customTabsIntent.intent.setPackage(forumTabHelper.getPackageName(getActivity()).get(0));
                    customTabsIntent.launchUrl(getActivity(), forumUri);
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ODK_FORUM)));
                }
                break;

            case KEY_OPEN_SOURCE_LICENSES:
                startActivity(new Intent(getActivity().getApplicationContext(),
                        OpenSourceLicensesActivity.class));
                break;

            case KEY_TELL_YOUR_FRIENDS:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.tell_your_friends_msg) + " " + GOOGLE_PLAY_URL
                                + APP_PACKAGE_NAME);
                startActivity(Intent.createChooser(shareIntent,
                        getString(R.string.tell_your_friends)));
                break;

            case KEY_LEAVE_A_REVIEW:
                boolean reviewTaken = false;
                try {
                    // Open the google play store app if present
                    Intent intent = new Intent(Intent.ACTION_VIEW,
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
                    Timber.e(anfe);
                }
                if (!reviewTaken) {
                    // Show a list of all available browsers if user doesn't have a default browser
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(GOOGLE_PLAY_URL + APP_PACKAGE_NAME));
                    startActivity(intent);
                }
                break;
        }
        return true;
    }
}