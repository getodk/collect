package org.odk.collect.android.projects

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.journeyapps.barcodescanner.BarcodeResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.fakes.FakePermissionsProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.permissions.PermissionsChecker
import org.odk.collect.android.permissions.PermissionsProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.CodeCaptureManagerFactory
import org.odk.collect.android.views.BarcodeViewDecoder
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.testshared.RobolectricHelpers
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class QrCodeProjectCreatorDialogTest {

    private val codeCaptureManagerFactory: CodeCaptureManagerFactory = mock {}
    private val permissionsProvider = FakePermissionsProvider()

    @Before
    fun setup() {
        permissionsProvider.setPermissionGranted(true)

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesCodeCaptureManagerFactory(): CodeCaptureManagerFactory {
                return codeCaptureManagerFactory
            }

            override fun providesPermissionsProvider(permissionsChecker: PermissionsChecker?): PermissionsProvider {
                return permissionsProvider
            }
        })
    }

    @Test
    fun `If camera permission is not granted the dialog should not be dismissed`() {
        permissionsProvider.setPermissionGranted(false)
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(QrCodeProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'Cancel' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(QrCodeProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.cancel)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(QrCodeProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The ManualProjectCreatorDialog should be displayed after switching to the manual mode`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(QrCodeProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withText(R.string.configure_manually)).perform(scrollTo(), click())
            assertThat(it.activity!!.supportFragmentManager.findFragmentByTag(ManualProjectCreatorDialog::class.java.name), `is`(notNullValue()))
        }
    }

    @Test
    fun `Successful project creation goes to main menu`() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesBarcodeViewDecoder(): BarcodeViewDecoder {
                val barcodeResult = mock<BarcodeResult> {
                    `when`(it.text).thenReturn("eJxLy88HAAKCAUU=")
                }

                return mock {
                    `when`(it.waitForBarcode(any())).thenReturn(MutableLiveData(barcodeResult))
                }
            }
            override fun providesProjectCreator(
                projectImporter: ProjectImporter?,
                projectsRepository: ProjectsRepository?,
                currentProjectProvider: CurrentProjectProvider?,
                settingsImporter: SettingsImporter?,
                context: Context,
                storagePathProvider: StoragePathProvider
            ): ProjectCreator {
                return mock {
                    `when`(it.createNewProject("foo")).thenReturn(true)
                }
            }
        })

        Intents.init()
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(QrCodeProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)

        scenario.onFragment {
            Intents.intended(IntentMatchers.hasComponent(MainMenuActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `When QR code is invalid a toast should be displayed`() {
        val projectCreator = mock<ProjectCreator>()

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesBarcodeViewDecoder(): BarcodeViewDecoder {
                val barcodeResult = mock<BarcodeResult> {
                    `when`(it.text).thenReturn("%")
                }

                return mock {
                    `when`(it.waitForBarcode(any())).thenReturn(MutableLiveData(barcodeResult))
                }
            }
            override fun providesProjectCreator(
                projectImporter: ProjectImporter?,
                projectsRepository: ProjectsRepository?,
                currentProjectProvider: CurrentProjectProvider?,
                settingsImporter: SettingsImporter?,
                context: Context,
                storagePathProvider: StoragePathProvider
            ): ProjectCreator {
                return projectCreator
            }
        })

        RobolectricHelpers.launchDialogFragmentInContainer(QrCodeProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        assertThat(ShadowToast.getTextOfLatestToast(), `is`(ApplicationProvider.getApplicationContext<Context>().getString(R.string.invalid_qrcode)))
        verifyNoInteractions(projectCreator)
    }
}
