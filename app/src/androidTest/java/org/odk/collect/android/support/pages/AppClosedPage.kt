package org.odk.collect.android.support.pages

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

class AppClosedPage : Page<AppClosedPage>() {

    override fun assertOnPage(): AppClosedPage {
        assertThat(currentActivity, equalTo(null))
        return this
    }
}
