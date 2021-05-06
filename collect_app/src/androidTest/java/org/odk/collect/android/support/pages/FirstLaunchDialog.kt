package org.odk.collect.android.support.pages

import androidx.test.rule.ActivityTestRule
import org.odk.collect.android.R

internal class FirstLaunchDialog(rule: ActivityTestRule<*>) : Page<FirstLaunchDialog>(rule) {
    override fun assertOnPage(): FirstLaunchDialog {
        assertText(R.string.configure_with_qr_code)
        assertText(R.string.configure_manually)
        assertText(R.string.configure_later)
        return this
    }
}