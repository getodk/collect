package org.odk.collect.android.feature.external

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.AppClosedPage
import org.odk.collect.android.support.pages.FormsDownloadResultPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.android.utilities.ApplicationConstants.BundleKeys.FORM_IDS
import org.odk.collect.android.utilities.ApplicationConstants.BundleKeys.PASSWORD
import org.odk.collect.android.utilities.ApplicationConstants.BundleKeys.SUCCESS_KEY
import org.odk.collect.android.utilities.ApplicationConstants.BundleKeys.URL
import org.odk.collect.android.utilities.ApplicationConstants.BundleKeys.USERNAME

@RunWith(AndroidJUnit4::class)
class FormDownloadActionTest {

    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)


    @Test
    fun passingIds_downloadsFormsFromProjectServer_andReturnsSuccessResult() {
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml")
        testDependencies.server.addForm("Two Question", "two_question", "1", "two-question.xml")

        val intent = Intent("org.odk.collect.android.FORM_DOWNLOAD")
        intent.type = FormsContract.CONTENT_TYPE
        intent.putExtra(FORM_IDS, arrayOf("one_question"))

        rule.withProject(testDependencies.server.url)
        val result = rule.launchForResult(intent, FormsDownloadResultPage()) {
            it.assertSuccess()
                .clickOK(AppClosedPage())
        }

        assertThat(result.resultCode, equalTo(Activity.RESULT_OK))
        assertThat(result.resultData.getBooleanExtra(SUCCESS_KEY, false), equalTo(true))
        assertThat(
            result.resultData.getSerializableExtra(FORM_IDS), equalTo(
                mapOf(
                    "one_question" to true
                )
            )
        )

        rule.relaunch(MainMenuPage())
            .clickFillBlankForm()
            .assertFormExists("One Question")
            .assertFormDoesNotExist("Two Question")
    }

    @Test
    fun passingIds_andServerDetails_downloadsFormsFromServer_andReturnsSuccessResult() {
        testDependencies.server.setCredentials("Pete", "meyre")
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml")
        testDependencies.server.addForm("Two Question", "two_question", "1", "two-question.xml")

        val intent = Intent("org.odk.collect.android.FORM_DOWNLOAD")
        intent.type = FormsContract.CONTENT_TYPE
        intent.putExtra(FORM_IDS, arrayOf("one_question"))
        intent.putExtra(URL, testDependencies.server.url)
        intent.putExtra(USERNAME, "Pete")
        intent.putExtra(PASSWORD, "meyre")

        rule.withProject("https://server2.example.com")
        val result = rule.launchForResult(intent, FormsDownloadResultPage()) {
            it.assertSuccess()
                .clickOK(AppClosedPage())
        }

        assertThat(result.resultCode, equalTo(Activity.RESULT_OK))
        assertThat(result.resultData.getBooleanExtra(SUCCESS_KEY, false), equalTo(true))
        assertThat(
            result.resultData.getSerializableExtra(FORM_IDS), equalTo(
                mapOf(
                    "one_question" to true
                )
            )
        )

        rule.relaunch(MainMenuPage())
            .clickFillBlankForm()
            .assertFormExists("One Question")
            .assertFormDoesNotExist("Two Question")
    }
}
