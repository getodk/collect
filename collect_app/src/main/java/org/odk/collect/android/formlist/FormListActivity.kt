package org.odk.collect.android.formlist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class FormListActivity : LocalizedActivity() {

    @Inject
    lateinit var viewModelFactory: FormListViewModel.Factory

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    private val viewModel: FormListViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_form_list)
        title = getString(R.string.enter_data)
        setSupportActionBar(findViewById(R.id.toolbar))

        viewModel.forms.observe(this) { forms ->
            findViewById<RecyclerView>(R.id.formList).apply {
                adapter = FormListAdapter(forms.value)
                layoutManager = LinearLayoutManager(context)
            }
        }

        viewModel.fetchForms()
    }
}
