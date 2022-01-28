package org.odk.collect.android.preferences.dialogs

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.preference.PreferenceDialogFragmentCompat
import org.odk.collect.android.R
import org.odk.collect.android.activities.CollectAbstractActivity
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.utilities.ProjectResetter
import org.odk.collect.android.utilities.ProjectResetter.ResetAction.RESET_PREFERENCES
import org.odk.collect.androidshared.ui.DialogFragmentUtils.dismissDialog
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

class ResetDialogPreferenceFragmentCompat :
    PreferenceDialogFragmentCompat(),
    CompoundButton.OnCheckedChangeListener {

    @JvmField
    @Inject
    var projectResetter: ProjectResetter? = null

    private var preferences: AppCompatCheckBox? = null
    private var instances: AppCompatCheckBox? = null
    private var forms: AppCompatCheckBox? = null
    private var layers: AppCompatCheckBox? = null
    private var cache: AppCompatCheckBox? = null
    private var _context: Context? = null

    override fun onAttach(context: Context) {
        this._context = context
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    public override fun onBindDialogView(view: View) {
        preferences = view.findViewById(R.id.preferences)
        instances = view.findViewById(R.id.instances)
        forms = view.findViewById(R.id.forms)
        layers = view.findViewById(R.id.layers)
        cache = view.findViewById(R.id.cache)
        preferences!!.setOnCheckedChangeListener(this)
        instances!!.setOnCheckedChangeListener(this)
        forms!!.setOnCheckedChangeListener(this)
        layers!!.setOnCheckedChangeListener(this)
        cache!!.setOnCheckedChangeListener(this)
        super.onBindDialogView(view)
    }

    override fun onStart() {
        super.onStart()
        adjustResetButtonAccessibility()
    }

    override fun onDetach() {
        preferences = null
        instances = null
        forms = null
        layers = null
        cache = null
        super.onDetach()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            resetSelected()
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {}
    private fun resetSelected() {
        val resetActions: MutableList<Int> = ArrayList()
        if (preferences!!.isChecked) {
            resetActions.add(RESET_PREFERENCES)
        }
        if (instances!!.isChecked) {
            resetActions.add(ProjectResetter.ResetAction.RESET_INSTANCES)
        }
        if (forms!!.isChecked) {
            resetActions.add(ProjectResetter.ResetAction.RESET_FORMS)
        }
        if (layers!!.isChecked) {
            resetActions.add(ProjectResetter.ResetAction.RESET_LAYERS)
        }
        if (cache!!.isChecked) {
            resetActions.add(ProjectResetter.ResetAction.RESET_CACHE)
        }
        if (!resetActions.isEmpty()) {
            object : AsyncTask<Void?, Void?, List<Int>>() {
                override fun onPreExecute() {
                    showIfNotShowing(
                        ResetProgressDialog::class.java,
                        (_context as CollectAbstractActivity?)!!.supportFragmentManager
                    )
                }

                override fun doInBackground(vararg voids: Void?): List<Int> {
                    return projectResetter!!.reset(resetActions)
                }

                override fun onPostExecute(failedResetActions: List<Int>) {
                    dismissDialog(
                        ResetProgressDialog::class.java,
                        (_context as CollectAbstractActivity?)!!.supportFragmentManager
                    )
                    handleResult(resetActions, failedResetActions)
                }
            }.execute()
        }
    }

    private fun handleResult(resetActions: List<Int>, failedResetActions: List<Int>) {
        val resultMessage = StringBuilder()
        for (action in resetActions) {
            when (action) {
                RESET_PREFERENCES -> if (failedResetActions.contains(action)) {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_settings_result),
                            _context!!.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_settings_result),
                            _context!!.getString(R.string.success)
                        )
                    )
                }
                ProjectResetter.ResetAction.RESET_INSTANCES -> if (failedResetActions.contains(
                        action
                    )
                ) {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_saved_forms_result),
                            _context!!.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_saved_forms_result),
                            _context!!.getString(R.string.success)
                        )
                    )
                }
                ProjectResetter.ResetAction.RESET_FORMS -> if (failedResetActions.contains(action)) {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_blank_forms_result),
                            _context!!.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_blank_forms_result),
                            _context!!.getString(R.string.success)
                        )
                    )
                }
                ProjectResetter.ResetAction.RESET_CACHE -> if (failedResetActions.contains(action)) {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_cache_result),
                            _context!!.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_cache_result),
                            _context!!.getString(R.string.success)
                        )
                    )
                }
                ProjectResetter.ResetAction.RESET_LAYERS -> if (failedResetActions.contains(action)) {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_layers_result),
                            _context!!.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context!!.getString(R.string.reset_layers_result),
                            _context!!.getString(R.string.success)
                        )
                    )
                }
            }
            if (resetActions.indexOf(action) < resetActions.size - 1) {
                resultMessage.append("\n\n")
            }
        }
        if (!(_context as CollectAbstractActivity?)!!.isInstanceStateSaved) {
            (_context as CollectAbstractActivity?)!!.runOnUiThread {
                if (resetActions.contains(RESET_PREFERENCES)) {
                    (_context as CollectAbstractActivity?)!!.recreate()
                }
                val resetSettingsResultDialog =
                    ResetSettingsResultDialog.newInstance(resultMessage.toString())
                try {
                    resetSettingsResultDialog.show(
                        (_context as CollectAbstractActivity?)!!.supportFragmentManager,
                        ResetSettingsResultDialog.RESET_SETTINGS_RESULT_DIALOG_TAG
                    )
                } catch (e: ClassCastException) {
                    Timber.i(e)
                }
            }
        }
        _context = null
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        adjustResetButtonAccessibility()
    }

    fun adjustResetButtonAccessibility() {
        if (preferences!!.isChecked || instances!!.isChecked || forms!!.isChecked
            || layers!!.isChecked || cache!!.isChecked
        ) {
            (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
            (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor((dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).currentTextColor)
        } else {
            (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                    getPartiallyTransparentColor(
                        (dialog as AlertDialog?)!!.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                        ).currentTextColor
                    )
                )
        }
    }

    private fun getPartiallyTransparentColor(color: Int): Int {
        return Color.argb(150, Color.red(color), Color.green(color), Color.blue(color))
    }

    companion object {
        fun newInstance(key: String?): ResetDialogPreferenceFragmentCompat {
            val fragment = ResetDialogPreferenceFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }
}
