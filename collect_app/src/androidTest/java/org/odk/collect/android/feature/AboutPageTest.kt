package org.odk.collect.android.feature

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.matchers.DrawableMatcher
import org.odk.collect.android.support.pages.AboutPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView

@RunWith(AndroidJUnit4::class)
class AboutPageTest {
    private var rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = chain().around(rule)

    @Test
    fun when_rotateScreenOnAboutPage_should_notCrash() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAbout()
            .rotateToLandscape(AboutPage())
            .assertOnPage()
            .scrollToOpenSourceLibrariesLicenses()
    }

    @Test
    fun when_openAboutPage_should_iconsBeVisible() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAbout()
            .assertOnPage()

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(0, R.id.title)
        ).check(matches(withText(org.odk.collect.strings.R.string.odk_website)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(0, R.id.summary)
        ).check(matches(withText(org.odk.collect.strings.R.string.odk_website_summary)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(0, R.id.imageView)
        ).check(matches(DrawableMatcher.withImageDrawable(R.drawable.ic_outline_website_24)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(1, R.id.title)
        ).check(matches(withText(org.odk.collect.strings.R.string.odk_forum)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(1, R.id.summary)
        ).check(matches(withText(org.odk.collect.strings.R.string.odk_forum_summary)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(1, R.id.imageView)
        ).check(matches(DrawableMatcher.withImageDrawable(R.drawable.ic_outline_forum_24)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(2, R.id.title)
        ).check(matches(withText(org.odk.collect.strings.R.string.tell_your_friends)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(2, R.id.summary)
        ).check(matches(withText(org.odk.collect.strings.R.string.tell_your_friends_msg)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(2, R.id.imageView)
        ).check(matches(DrawableMatcher.withImageDrawable(R.drawable.ic_outline_share_24)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(3, R.id.title)
        ).check(matches(withText(org.odk.collect.strings.R.string.leave_a_review)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(3, R.id.summary)
        ).check(matches(withText(org.odk.collect.strings.R.string.leave_a_review_msg)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(3, R.id.imageView)
        ).check(matches(DrawableMatcher.withImageDrawable(R.drawable.ic_outline_rate_review_24)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(4, R.id.title)
        ).check(matches(withText(org.odk.collect.strings.R.string.all_open_source_licenses)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(4, R.id.summary)
        ).check(matches(withText(org.odk.collect.strings.R.string.all_open_source_licenses_msg)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(4, R.id.imageView)
        ).check(matches(DrawableMatcher.withImageDrawable(R.drawable.ic_outline_stars_24)))
    }

    @Test
    fun when_OpenSourcesLibrariesLicenses_should_openSourceLicensesTitleBeDisplayed() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAbout()
            .clickOnOpenSourceLibrariesLicenses()
    }
}
