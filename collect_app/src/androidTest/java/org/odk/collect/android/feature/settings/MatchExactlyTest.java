package org.odk.collect.android.feature.settings;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.ResetStateRule;

@RunWith(AndroidJUnit4.class)
public class MatchExactlyTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule())
            .around(rule);

    @Test
    public void whenMatchExactlyEnabled_getBlankFormsButtonIsGone() {
        rule.mainMenu()
                .enableMatchExactly()
                .assertTextNotDisplayed(R.string.get_forms);
    }

}
