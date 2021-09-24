package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.AboutPage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.odk.collect.android.support.matchers.DrawableMatcher.withImageDrawable;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;

//Issue NODK-234
@RunWith(AndroidJUnit4.class)
public class AboutPageTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain ruleChain = TestRuleChain.chain()
            .around(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(rule);

    @Test
    public void when_rotateScreenOnAboutPage_should_notCrash() {
        //TestCase1
        rule.startAtMainMenu()
                .openProjectSettings()
                .clickAbout()
                .rotateToLandscape(new AboutPage())
                .assertOnPage()
                .scrollToOpenSourceLibrariesLicenses();
    }

    @Test
    public void when_openAboutPage_should_iconsBeVisible() {
        //TestCase2
        rule.startAtMainMenu()
                .openProjectSettings()
                .clickAbout()
                .assertOnPage();

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(0, R.id.title))
                .check(matches(withText(R.string.odk_website)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(0, R.id.summary))
                .check(matches(withText(R.string.odk_website_summary)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(0, R.id.imageView))
                .check(matches(withImageDrawable(R.drawable.ic_outline_website_24)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(1, R.id.title))
                .check(matches(withText(R.string.odk_forum)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(1, R.id.summary))
                .check(matches(withText(R.string.odk_forum_summary)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(1, R.id.imageView))
                .check(matches(withImageDrawable(R.drawable.ic_outline_forum_24)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(2, R.id.title))
                .check(matches(withText(R.string.tell_your_friends)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(2, R.id.summary))
                .check(matches(withText(R.string.tell_your_friends_msg)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(2, R.id.imageView))
                .check(matches(withImageDrawable(R.drawable.ic_outline_share_24)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(3, R.id.title))
                .check(matches(withText(R.string.leave_a_review)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(3, R.id.summary))
                .check(matches(withText(R.string.leave_a_review_msg)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(3, R.id.imageView))
                .check(matches(withImageDrawable(R.drawable.ic_outline_rate_review_24)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(4, R.id.title))
                .check(matches(withText(R.string.all_open_source_licenses)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(4, R.id.summary))
                .check(matches(withText(R.string.all_open_source_licenses_msg)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(4, R.id.imageView))
                .check(matches(withImageDrawable(R.drawable.ic_outline_stars_24)));
    }

    @Test
    public void when_OpenSourcesLibrariesLicenses_should_openSourceLicensesTitleBeDisplayed() {
        //TestCase3
        rule.startAtMainMenu()
                .openProjectSettings()
                .clickAbout()
                .clickOnOpenSourceLibrariesLicenses();
    }
}
