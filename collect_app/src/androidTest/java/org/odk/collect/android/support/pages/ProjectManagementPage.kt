package org.odk.collect.android.support.pages

class ProjectManagementPage : Page<ProjectManagementPage>() {

    override fun assertOnPage(): ProjectManagementPage {
        assertText(org.odk.collect.strings.R.string.project_management_section_title)
        return this
    }

    fun clickOnResetProject(): ProjectManagementPage {
        clickOnString(org.odk.collect.strings.R.string.reset_project_settings_title)
        return this
    }

    fun clickConfigureQR(): QRCodePage {
        clickOnString(org.odk.collect.strings.R.string.reconfigure_with_qr_code_settings_title)
        return QRCodePage().assertOnPage()
    }

    fun deleteProject(): MainMenuPage {
        scrollToRecyclerViewItemAndClickText(org.odk.collect.strings.R.string.delete_project)
        inputText(getTranslatedString(org.odk.collect.strings.R.string.delete_trigger))
        clickOnString(org.odk.collect.strings.R.string.delete_project_confirm_button_text)
        return MainMenuPage()
    }

    fun deleteLastProject(): FirstLaunchPage {
        scrollToRecyclerViewItemAndClickText(org.odk.collect.strings.R.string.delete_project)
        inputText("delete")
        clickOnString(org.odk.collect.strings.R.string.delete_project_confirm_button_text)
        return FirstLaunchPage()
    }
}
