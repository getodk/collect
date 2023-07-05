package org.odk.collect.android.support.pages

import org.odk.collect.android.R

class ProjectManagementPage : Page<ProjectManagementPage>() {

    override fun assertOnPage(): ProjectManagementPage {
        assertText(org.odk.collect.strings.R.string.project_management_section_title)
        return this
    }

    fun clickOnResetApplication(): ProjectManagementPage {
        clickOnString(org.odk.collect.strings.R.string.reset_project_settings_title)
        return this
    }

    fun clickConfigureQR(): QRCodePage {
        clickOnString(org.odk.collect.strings.R.string.reconfigure_with_qr_code_settings_title)
        return QRCodePage().assertOnPage()
    }

    fun clickOnDeleteProject(): ProjectManagementPage {
        scrollToRecyclerViewItemAndClickText(org.odk.collect.strings.R.string.delete_project)
        return this
    }

    fun deleteProject(): MainMenuPage {
        scrollToRecyclerViewItemAndClickText(org.odk.collect.strings.R.string.delete_project)
        clickOnString(org.odk.collect.strings.R.string.delete_project_yes)
        return MainMenuPage()
    }

    fun deleteLastProject(): FirstLaunchPage {
        scrollToRecyclerViewItemAndClickText(org.odk.collect.strings.R.string.delete_project)
        clickOnString(org.odk.collect.strings.R.string.delete_project_yes)
        return FirstLaunchPage()
    }
}
