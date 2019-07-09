package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.odk.collect.android.activities.MainMenuActivity;

public class BaseRegressionTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> main = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
}