package org.odk.collect.android.support.pages

import org.odk.collect.android.R

class ProjectManagementPage : Page<ProjectManagementPage>() {

    override fun assertOnPage(): ProjectManagementPage {
        assertText(R.string.project_management_section_title)
        return this
    }

    fun clickOnResetApplication(): ProjectManagementPage {
        clickOnString(R.string.reset_project_settings_title)
        return this
    }

    fun clickConfigureQR(): QRCodePage {
        clickOnString(R.string.reconfigure_with_qr_code_settings_title)
        return QRCodePage().assertOnPage()
    }

    fun deleteProject(): MainMenuPage {
        scrollToRecyclerViewItemAndClickText(R.string.delete_project)
        clickOnString(R.string.delete_project_yes)
        return MainMenuPage()
    }

    fun deleteLastProject(): FirstLaunchPage {
        scrollToRecyclerViewItemAndClickText(R.string.delete_project)
        clickOnString(R.string.delete_project_yes)
        return FirstLaunchPage()
    }
}
