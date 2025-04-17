package org.odk.collect.android.external

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.formlists.blankformlist.BlankFormListItem
import org.odk.collect.android.formlists.blankformlist.BlankFormListViewModel
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.async.Scheduler
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.testshared.Assertions

@RunWith(AndroidJUnit4::class)
class AndroidShortcutsActivityTest {
    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Test
    fun `new forms are displayed when the list of forms gets updated`() {
        val forms = MutableLiveData<List<BlankFormListItem>>()

        val viewmodel = mock<BlankFormListViewModel>().apply {
            whenever(formsToDisplay).thenReturn(forms)
        }

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesBlankFormListViewModel(
                formsRepositoryProvider: FormsRepositoryProvider,
                instancesRepositoryProvider: InstancesRepositoryProvider,
                application: Application,
                formsDataService: FormsDataService,
                scheduler: Scheduler,
                settingsProvider: SettingsProvider,
                changeLockProvider: ChangeLockProvider,
                projectsDataService: ProjectsDataService
            ): BlankFormListViewModel.Factory {
                return object : BlankFormListViewModel.Factory(
                    InMemInstancesRepository(),
                    application,
                    formsDataService,
                    scheduler,
                    settingsProvider.getUnprotectedSettings(),
                    ""
                ) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return viewmodel as T
                    }
                }
            }
        })

        launcherRule.launch(AndroidShortcutsActivity::class.java)

        val formItem1 = BlankFormListItem(1, "1", "Test form 1", "1", "", 0, 0, null, Uri.parse(""))
        forms.value = listOf(formItem1)
        Assertions.assertText(withText(formItem1.formName), isDialog())

        val formItem2 = BlankFormListItem(2, "1", "Test form 2", "1", "", 0, 0, null, Uri.parse(""))
        forms.value = listOf(formItem1, formItem2)
        Assertions.assertText(withText(formItem1.formName), isDialog())
        Assertions.assertText(withText(formItem2.formName), isDialog())
    }
}
