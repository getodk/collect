package org.odk.collect.android.feature.formentry

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.FileUtils
import org.odk.collect.android.support.ResetStateRule
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.actions.NestedScrollToAction.nestedScrollTo
import java.io.File

class ImageWidgetTest {
    var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(ResetStateRule())
        .around(rule)

    @Test // https://github.com/getodk/collect/issues/4819
    fun attachingGifsShouldBePossible() {
        Intents.init()
        val resultIntent = Intent()
        val file = File.createTempFile("file", "gif", File(StoragePathProvider().odkRootDirPath))
        FileUtils.copyFileFromAssets("media" + File.separator + "file.gif", file.path)
        resultIntent.data = FileProvider.getUriForFile(
            Collect.getInstance(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

        intending(not(IntentMatchers.isInternal())).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent))

        rule.startAtMainMenu()
            .copyForm("image_widget.xml")
            .startBlankForm("image_widget")
            .clickOnString(R.string.choose_image)

        onView(withTagValue(`is`("ImageView")))
            .perform(nestedScrollTo())
            .check(matches(isDisplayed()))
    }
}
