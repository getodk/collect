package org.odk.collect.android.feature.storage;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.AppStateProvider;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AutomaticStorageMigrationTest {

    public CollectTestRule rule = new CollectTestRule();

    final TestDependencies testDependencies = new TestDependencies() {
        @Override
        public AppStateProvider providesAppStateProvider() {
            AppStateProvider appStateProvider = spy(new AppStateProvider());
            when(appStateProvider.isFreshInstall(any())).thenReturn(false);
            return appStateProvider;
        }
    };

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(false, testDependencies)
            .around(rule);

    @Test
    public void when_storageMigrationNotPerformed_shouldBePerformedAutomatically() {
        new MainMenuPage(rule)
                .assertStorageMigrationCompletedBannerIsDisplayed();
    }
}
