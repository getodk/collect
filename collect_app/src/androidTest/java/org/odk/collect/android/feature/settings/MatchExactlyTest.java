package org.odk.collect.android.feature.settings;

import android.Manifest;
import android.webkit.MimeTypeMap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.CountingScheduler;
import org.odk.collect.android.support.CountingSchedulerIdlingResource;
import org.odk.collect.android.support.IdlingResourceRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.StubOpenRosaServer;
import org.odk.collect.async.CoroutineScheduler;
import org.odk.collect.async.Scheduler;
import org.odk.collect.utilities.UserAgentProvider;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MatchExactlyTest {

    public final StubOpenRosaServer server = new StubOpenRosaServer();
    private final CountingScheduler countingScheduler = new CountingScheduler(new CoroutineScheduler());

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule(new AppDependencyModule() {
                @Override
                public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
                    return server;
                }

                @Override
                public Scheduler providesScheduler() {
                    return countingScheduler;
                }
            }))
            .around(new IdlingResourceRule(new CountingSchedulerIdlingResource(countingScheduler)))
            .around(new CopyFormRule("one-question.xml"))
            .around(new CopyFormRule("one-question-repeat.xml"))
            .around(rule);

    @Test
    public void whenMatchExactlyEnabled_clickingFillBlankForm_andClickingRefresh_getsLatestFormsFromServer() {
        server.addForm("One Question Updated", "one_question", "one-question-updated.xml");
        server.addForm("Two Question", "two_question", "two-question.xml");

        rule.mainMenu()
                .setServer(server.getURL())
                .enableMatchExactly()
                .clickFillBlankForm()
                .assertText("One Question")
                .assertText("One Question Repeat")
                .clickRefresh()
                .assertText("Two Question") // Check new form downloaded
                .assertText("One Question Updated") // Check updated form updated
                .assertTextDoesNotExist("One Question Repeat"); // Check deleted form deleted
    }

    @Test
    public void whenMatchExactlyEnabled_getBlankFormsButtonIsGone() {
        rule.mainMenu()
                .enableMatchExactly()
                .assertTextNotDisplayed(R.string.get_forms);
    }

    @Test
    public void whenMatchExactlyEnabled_formManagementFormUpdateIsDisabled() {
        rule.mainMenu()
                .enableMatchExactly()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .assertDisabled(R.string.periodic_form_updates_check_title)
                .assertDisabled(R.string.automatic_download)
                .assertDisabled(R.string.hide_old_form_versions_setting_title);
    }

    @Test
    public void whenMatchExactlyNotEnabled_fillBlankFormRefreshButtonIsGone() {
        rule.mainMenu()
                .clickFillBlankForm();

        onView(withId(R.id.menu_refresh)).check(doesNotExist());
    }
}
