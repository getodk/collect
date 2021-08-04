package org.odk.collect.android.feature.projects

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage

@RunWith(AndroidJUnit4::class)
class ProjectsAdbTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Test
    fun clearingStorage_andReturningToApp_recreatesStorageForProject() {
        val fillBlankFormPage = rule.startAtMainMenu().clickFillBlankForm()

        val storage = getApplicationContext<Application>().getExternalFilesDir(null)
        storage!!.listFiles()!!.forEach { it.deleteRecursively() }

        fillBlankFormPage.pressBack(MainMenuPage())
            .assertProjectIcon("D")
    }
}
