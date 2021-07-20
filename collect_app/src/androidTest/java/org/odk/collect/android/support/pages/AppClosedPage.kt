package org.odk.collect.android.support.pages

import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers

class AppClosedPage : Page<AppClosedPage>() {

    override fun assertOnPage(): AppClosedPage {
        ViewMatchers.assertThat(getCurrentActivity(), CoreMatchers.equalTo(null))
        return this
    }
}
