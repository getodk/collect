package org.odk.collect.android.configure.qr

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.application.initialization.AnalyticsInitializer
import org.odk.collect.android.application.initialization.MapsInitializer
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.async.Scheduler
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.testshared.FakeBarcodeScannerViewFactory
import org.odk.collect.testshared.FakeScheduler
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class QRCodeScannerFragmentTest {
    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val barcodeScannerViewFactory = FakeBarcodeScannerViewFactory()
    private val scheduler = FakeScheduler()

    @Before
    fun setup() {
        Intents.init()

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesCurrentProjectProvider(
                application: Application,
                settingsProvider: SettingsProvider,
                projectsRepository: ProjectsRepository,
                analyticsInitializer: AnalyticsInitializer,
                context: Context,
                mapsInitializer: MapsInitializer
            ): ProjectsDataService {
                return mock<ProjectsDataService> {
                    on { requireCurrentProject() } doReturn Project.DEMO_PROJECT
                }
            }

            override fun providesBarcodeScannerViewFactory(settingsProvider: SettingsProvider): BarcodeScannerViewContainer.Factory {
                return barcodeScannerViewFactory
            }

            override fun providesScheduler(workManager: WorkManager): Scheduler {
                return scheduler
            }
        })
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `When valid settings passed stop scanning and navigate to the main menu`() {
        launcherRule.launch(QRCodeScannerFragment::class.java)
        barcodeScannerViewFactory.scan(
            """
                {
                    "general": {},
                    "admin": {}
                }
                """
        )
        assertThat(barcodeScannerViewFactory.isScanning, equalTo(false))
        assertThat(
            ShadowToast.getTextOfLatestToast(),
            equalTo(context.getString(org.odk.collect.strings.R.string.successfully_imported_settings))
        )
        assertThat(
            Intents.getIntents()[0],
            hasComponent(MainMenuActivity::class.java.name)
        )
    }

    @Test
    fun `When invalid settings passed display an error and keep scanning`() {
        launcherRule.launch(QRCodeScannerFragment::class.java)
        barcodeScannerViewFactory.scan("{}")
        scheduler.runForeground()
        assertThat(barcodeScannerViewFactory.isScanning, equalTo(true))
        assertThat(
            ShadowToast.getTextOfLatestToast(),
            equalTo(context.getString(org.odk.collect.strings.R.string.invalid_qrcode))
        )
        assertThat(Intents.getIntents().isEmpty(), equalTo(true))
    }

    @Test
    fun `When settings with GD project passed display an error and keep scanning`() {
        launcherRule.launch(QRCodeScannerFragment::class.java)
        barcodeScannerViewFactory.scan(
            """
                {
                    "general": {
                        "protocol": "google_sheets"
                    },
                    "admin": {}
                }
                """
        )
        scheduler.runForeground()
        assertThat(barcodeScannerViewFactory.isScanning, equalTo(true))
        assertThat(
            ShadowToast.getTextOfLatestToast(),
            equalTo(context.getString(org.odk.collect.strings.R.string.settings_with_gd_protocol))
        )
        assertThat(Intents.getIntents().isEmpty(), equalTo(true))
    }
}
