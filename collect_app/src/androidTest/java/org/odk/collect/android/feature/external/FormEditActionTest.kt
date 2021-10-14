package org.odk.collect.android.feature.external

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.ContentProviderUtils
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.AppClosedPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.OkDialog

@RunWith(AndroidJUnit4::class)
class FormEditActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun opensForm() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        val uri = FormsContract.getUri("DEMO", formId)

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        rule.launch(intent, FormEntryPage("One Question"))
    }

    @Test
    fun whenFormIsNotCurrentProject_showsWarningAndExits() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")
            .addAndSwitchToProject("https://example.com")

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        val uri = FormsContract.getUri("DEMO", formId)

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        rule.launch(intent, OkDialog())
            .assertText(R.string.wrong_project_selected_for_form)
            .clickOK(AppClosedPage())
    }

    @Test
    fun whenUriDoesNotHaveProjectId_andCurrentProjectIsFirstOne_opensForm() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")
            .addAndSwitchToProject("https://example.com")
            .openProjectSettingsDialog()
            .selectProject("Demo project")

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        val uri = FormsContract.getUri("DEMO", formId)
        val uriWithoutProjectId = Uri.Builder()
            .scheme(uri.scheme)
            .authority(uri.authority)
            .path(uri.path)
            .query(null)
            .build()

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uriWithoutProjectId }
        rule.launch(intent, FormEntryPage("One Question"))
    }

    @Test
    fun whenUriDoesNotHaveProjectId_andCurrentProjectIsNotFirstOne_showsWarningAndExits() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")
            .addAndSwitchToProject("https://example.com")

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        val uri = FormsContract.getUri("DEMO", formId)
        val uriWithoutProjectId = Uri.Builder()
            .scheme(uri.scheme)
            .authority(uri.authority)
            .path(uri.path)
            .query(null)
            .build()

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uriWithoutProjectId }
        rule.launch(intent, OkDialog())
            .assertText(R.string.wrong_project_selected_for_form)
            .clickOK(AppClosedPage())
    }
}
