package org.odk.collect.android.feature.settings;

import android.Manifest;
import android.webkit.MimeTypeMap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.work.WorkManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.FormLoadingUtils;
import org.odk.collect.android.support.IdlingResourceRule;
import org.odk.collect.android.support.NotificationDrawerRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.SchedulerIdlingResource;
import org.odk.collect.android.support.StubOpenRosaServer;
import org.odk.collect.android.support.TestScheduler;
import org.odk.collect.android.support.pages.FormManagementPage;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.NotificationDrawer;
import org.odk.collect.async.Scheduler;
import org.odk.collect.utilities.UserAgentProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class FormManagementSettingsTest {

    private final StubOpenRosaServer server = new StubOpenRosaServer();
    private final TestScheduler testScheduler = new TestScheduler();
    private final NotificationDrawerRule notificationDrawer = new NotificationDrawerRule();


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
                public Scheduler providesScheduler(WorkManager workManager) {
                    return testScheduler;
                }

                @Override
                public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
                    return server;
                }
            }))
            .around(notificationDrawer)
            .around(new IdlingResourceRule(new SchedulerIdlingResource(testScheduler)))
            .around(rule);

    @Test
    public void whenManualUpdatesEnabled_disablesPrefs() {
        rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.manually)
                .assertDisabled(R.string.form_update_frequency_title)
                .assertDisabled(R.string.automatic_download);
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_disablesPrefs() {
        rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.previously_downloaded_only)
                .assertEnabled(R.string.form_update_frequency_title)
                .assertEnabled(R.string.automatic_download);
    }

    @Test
    public void whenMatchExactlyEnabled_disablesPrefs() {
        rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.match_exactly)
                .assertEnabled(R.string.form_update_frequency_title)
                .assertDisabled(R.string.automatic_download);
    }

    @Test
    public void whenMatchExactlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        List<TestScheduler.DeferredTask> deferredTasks = testScheduler.getDeferredTasks();
        assertThat(deferredTasks, is(empty()));

        FormManagementPage page = rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.match_exactly);

        deferredTasks = testScheduler.getDeferredTasks();
        assertThat(deferredTasks.size(), is(1));
        String matchExactlyTag = deferredTasks.get(0).getTag();

        page.clickAutomaticUpdateFrequency()
                .clickOption(R.string.every_one_hour);

        deferredTasks = testScheduler.getDeferredTasks();
        assertThat(deferredTasks.size(), is(1));
        assertThat(deferredTasks.get(0).getTag(), is(matchExactlyTag));
        assertThat(deferredTasks.get(0).getRepeatPeriod(), is(1000L * 60 * 60));
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        List<TestScheduler.DeferredTask> deferredTasks = testScheduler.getDeferredTasks();
        assertThat(deferredTasks, is(empty()));

        FormManagementPage page = rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.previously_downloaded_only);

        deferredTasks = testScheduler.getDeferredTasks();
        assertThat(deferredTasks.size(), is(1));
        String previouslyDownloadedTag = deferredTasks.get(0).getTag();

        page.clickAutomaticUpdateFrequency()
                .clickOption(R.string.every_one_hour);

        deferredTasks = testScheduler.getDeferredTasks();
        assertThat(deferredTasks.size(), is(1));
        assertThat(deferredTasks.get(0).getTag(), is(previouslyDownloadedTag));
        assertThat(deferredTasks.get(0).getRepeatPeriod(), is(1000L * 60 * 60));
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_checkingAutoDownload_downloadsUpdatedForms() throws Exception {
        FormManagementPage page = rule.mainMenu()
                .setServer(server.getURL())
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.previously_downloaded_only)
                .clickOnString(R.string.automatic_download);

        FormLoadingUtils.copyFormToStorage("one-question.xml");
        server.addForm("One Question Updated", "one_question", "one-question-updated.xml");
        testScheduler.runDeferredTasks();

        page.pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickFillBlankForm()
                .assertText("One Question Updated");

        notificationDrawer.open()
                .assertNotification("ODK Collect", "ODK auto-download results", "Success");
    }
}
