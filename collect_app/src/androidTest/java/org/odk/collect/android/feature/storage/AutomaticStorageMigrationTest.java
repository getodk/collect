package org.odk.collect.android.feature.storage;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class AutomaticStorageMigrationTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(false, new TestDependencies())
            .around(rule);

    @Test
    public void when_storageMigrationNotPerformed_shouldBePerformedAutomatically() {
        new MainMenuPage(rule)
                .assertStorageMigrationCompletedBannerIsDisplayed();
    }
}
