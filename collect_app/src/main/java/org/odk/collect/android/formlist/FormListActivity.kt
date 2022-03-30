package org.odk.collect.android.formlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.activities.FormMapActivity
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.android.preferences.dialogs.ServerAuthDialogFragment
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.SnackbarUtils
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class FormListActivity : LocalizedActivity(), OnFormItemClickListener {

    @Inject
    lateinit var viewModelFactory: FormListViewModel.Factory

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    private val viewModel: FormListViewModel by viewModels { viewModelFactory }

    private lateinit var menuDelegate: FormListMenuDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_form_list)
        title = getString(R.string.enter_data)
        setSupportActionBar(findViewById(R.id.toolbar))

        menuDelegate = FormListMenuDelegate(this, viewModel, networkStateProvider)

        val formListAdapter = FormListAdapter().apply {
            listener = this@FormListActivity
        }

        findViewById<RecyclerView>(R.id.formList).apply {
            adapter = formListAdapter
            layoutManager = LinearLayoutManager(context)
            val itemDecoration = DividerItemDecoration(this@FormListActivity, DividerItemDecoration.VERTICAL)
            itemDecoration.setDrawable(ContextCompat.getDrawable(this@FormListActivity, R.drawable.list_item_divider)!!)
            addItemDecoration(itemDecoration)
        }

        viewModel.showProgressBar.observe(this) { shouldShowProgressBar ->
            findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (shouldShowProgressBar) View.VISIBLE
                else View.GONE
        }

        viewModel.syncResult.observe(this) { result ->
            if (!result.isConsumed()) {
                SnackbarUtils.showShortSnackbar(findViewById(R.id.formList), result.value)
            }
        }

        viewModel.formsToDisplay.observe(this) { forms ->
            findViewById<RecyclerView>(R.id.formList).visibility = if (forms.isEmpty()) View.GONE else View.VISIBLE
            findViewById<TextView>(R.id.empty_list_message).visibility = if (forms.isEmpty()) View.VISIBLE else View.GONE
            formListAdapter.setData(forms)
        }

        viewModel.isSyncingWithServer().observe(this) { syncing: Boolean ->
            findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (syncing) View.VISIBLE
                else View.GONE
        }

        viewModel.isAuthenticationRequired().observe(this) { authenticationRequired ->
            if (authenticationRequired) {
                DialogFragmentUtils.showIfNotShowing(
                    ServerAuthDialogFragment::class.java,
                    supportFragmentManager
                )
            } else {
                DialogFragmentUtils.dismissDialog(
                    ServerAuthDialogFragment::class.java,
                    supportFragmentManager
                )
            }
        }
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
        return menuDelegate.onOptionsItemSelected(item)
    }

    override fun onFormClick(formUri: Uri) {
        if (Intent.ACTION_PICK == intent.action) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, Intent().setData(formUri))
        } else {
            // caller wants to view/edit a form, so launch formentryactivity
            Intent(this, FormEntryActivity::class.java).apply {
                action = Intent.ACTION_EDIT
                data = formUri
                putExtra(
                    ApplicationConstants.BundleKeys.FORM_MODE,
                    ApplicationConstants.FormModes.EDIT_SAVED
                )

                startActivity(this)
            }
        }
        finish()
    }

    override fun onMapButtonClick(id: Long) {
        permissionsProvider.requestEnabledLocationPermissions(
            this,
            object : PermissionListener {
                override fun granted() {
                    startActivity(
                        Intent(this@FormListActivity, FormMapActivity::class.java).also {
                            it.putExtra(FormMapActivity.EXTRA_FORM_ID, id)
                        }
                    )
                }

                override fun denied() = Unit
            }
        )
    }
}
