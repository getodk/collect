package org.odk.collect.android.preferences

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ProjectPreferencesViewModelTest {

    private lateinit var projectPreferencesViewModel: ProjectPreferencesViewModel
    private val adminPasswordProvider = mock<AdminPasswordProvider>()

    @Test
    fun `When admin password is set the initial stat should be Locked`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(true)

        projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)
        RobolectricHelpers.runLooper()

        assertThat(projectPreferencesViewModel.state.value, `is`(Consumable(ProjectPreferencesViewModel.State.LOCKED)))
    }

    @Test
    fun `When admin password is not set the initial stat should be NotProtected`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(false)

        projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)
        RobolectricHelpers.runLooper()

        assertThat(projectPreferencesViewModel.state.value, `is`(Consumable(ProjectPreferencesViewModel.State.NOT_PROTECTED)))
    }

    @Test
    fun `setStateLocked() should set state to Locked`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(false)

        projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)
        projectPreferencesViewModel.setStateLocked()
        RobolectricHelpers.runLooper()

        assertThat(projectPreferencesViewModel.state.value, `is`(Consumable(ProjectPreferencesViewModel.State.LOCKED)))
    }

    @Test
    fun `setStateUnlocked() should set state to Unlocked`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(false)

        projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)
        projectPreferencesViewModel.setStateUnlocked()
        RobolectricHelpers.runLooper()

        assertThat(projectPreferencesViewModel.state.value, `is`(Consumable(ProjectPreferencesViewModel.State.UNLOCKED)))
    }

    @Test
    fun `setStateNotProtected() should set state to NotProtected`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(false)

        projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)
        projectPreferencesViewModel.setStateNotProtected()
        RobolectricHelpers.runLooper()

        assertThat(projectPreferencesViewModel.state.value, `is`(Consumable(ProjectPreferencesViewModel.State.NOT_PROTECTED)))
    }

    @Test
    fun `isStateLocked() should return true if state is Locked`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(false)

        projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)

        projectPreferencesViewModel.setStateLocked()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateLocked(), `is`(true))

        projectPreferencesViewModel.setStateUnlocked()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateLocked(), `is`(false))

        projectPreferencesViewModel.setStateNotProtected()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateLocked(), `is`(false))
    }

    @Test
    fun `isStateUnlocked() should return true if state is Unlocked`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(false)

        projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)

        projectPreferencesViewModel.setStateLocked()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateUnlocked(), `is`(false))

        projectPreferencesViewModel.setStateUnlocked()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateUnlocked(), `is`(true))

        projectPreferencesViewModel.setStateNotProtected()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateUnlocked(), `is`(false))
    }

    @Test
    fun `isStateNotProtected() should return true if state is NotProtected`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(false)

        projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)

        projectPreferencesViewModel.setStateLocked()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateNotProtected(), `is`(false))

        projectPreferencesViewModel.setStateUnlocked()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateNotProtected(), `is`(false))

        projectPreferencesViewModel.setStateNotProtected()
        RobolectricHelpers.runLooper()
        assertThat(projectPreferencesViewModel.isStateNotProtected(), `is`(true))
    }
}
