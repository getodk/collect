package org.odk.collect.android.formlist

import android.os.Bundle
import org.odk.collect.android.R
import org.odk.collect.strings.localization.LocalizedActivity

class FormListActivity : LocalizedActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_list)
        title = getString(R.string.enter_data)
        setSupportActionBar(findViewById(R.id.toolbar))
    }
}
