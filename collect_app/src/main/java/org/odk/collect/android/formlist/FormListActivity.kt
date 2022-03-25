package org.odk.collect.android.formlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class FormListActivity : LocalizedActivity() {

    @Inject
    lateinit var viewModelFactory: FormListViewModel.Factory

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    private val viewModel: FormListViewModel by viewModels { viewModelFactory }

    private lateinit var menuDelegate: FormListMenuDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_form_list)
        title = getString(R.string.enter_data)
        setSupportActionBar(findViewById(R.id.toolbar))

        menuDelegate = FormListMenuDelegate(this, viewModel, networkStateProvider)

        viewModel.forms.observe(this) { forms ->
            findViewById<RecyclerView>(R.id.formList).apply {
                adapter = FormListAdapter(forms.value)
                layoutManager = LinearLayoutManager(context)
            }
        }

        viewModel.sortingOrder.observe(this) { sortingOrder ->
            // update adapter
        }

        viewModel.filterText.observe(this) { filterText ->
            // update adapter
        }

        viewModel.fetchForms()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuDelegate.onCreateOptionsMenu(menuInflater, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuDelegate.onPrepareOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (!MultiClickGuard.allowClick(javaClass.name)) {
            true
        } else {
            menuDelegate.onOptionsItemSelected(item)
        }
    }
}
