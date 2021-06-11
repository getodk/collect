package org.odk.collect.android.support.pages

import org.odk.collect.android.R

internal class FirstLaunchDialogPage : Page<FirstLaunchDialogPage>() {

    override fun assertOnPage(): FirstLaunchDialogPage {
        assertText(R.string.configure_with_qr_code)
        assertText(R.string.configure_manually)
        assertText(R.string.tagline)
        assertText(R.string.try_demo)
        return this
    }
}
