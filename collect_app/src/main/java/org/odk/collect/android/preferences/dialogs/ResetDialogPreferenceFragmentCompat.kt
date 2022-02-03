package org.odk.collect.android.preferences.dialogs

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
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
import org.odk.collect.android.utilities.ProjectResetter.ResetAction.RESET_CACHE
import org.odk.collect.android.utilities.ProjectResetter.ResetAction.RESET_FORMS
import org.odk.collect.android.utilities.ProjectResetter.ResetAction.RESET_INSTANCES
import org.odk.collect.android.utilities.ProjectResetter.ResetAction.RESET_LAYERS
import org.odk.collect.android.utilities.ProjectResetter.ResetAction.RESET_PREFERENCES
import org.odk.collect.androidshared.ui.DialogFragmentUtils.dismissDialog
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.async.Scheduler
import org.odk.collect.strings.localization.getLocalizedString
import timber.log.Timber
import javax.inject.Inject

class ResetDialogPreferenceFragmentCompat :
    PreferenceDialogFragmentCompat(),
    CompoundButton.OnCheckedChangeListener {

    @Inject
    lateinit var projectResetter: ProjectResetter

    @Inject
    lateinit var scheduler: Scheduler

    private var preferences: AppCompatCheckBox? = null
    private var instances: AppCompatCheckBox? = null
    private var forms: AppCompatCheckBox? = null
    private var layers: AppCompatCheckBox? = null
    private var cache: AppCompatCheckBox? = null

    private lateinit var _context: Context

    override fun onAttach(context: Context) {
        _context = context
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    public override fun onBindDialogView(view: View) {
        preferences = view.findViewById<AppCompatCheckBox>(R.id.preferences).apply {
            setOnCheckedChangeListener(this@ResetDialogPreferenceFragmentCompat)
        }
        instances = view.findViewById<AppCompatCheckBox>(R.id.instances).apply {
            setOnCheckedChangeListener(this@ResetDialogPreferenceFragmentCompat)
        }
        forms = view.findViewById<AppCompatCheckBox>(R.id.forms).apply {
            setOnCheckedChangeListener(this@ResetDialogPreferenceFragmentCompat)
        }
        layers = view.findViewById<AppCompatCheckBox>(R.id.layers).apply {
            setOnCheckedChangeListener(this@ResetDialogPreferenceFragmentCompat)
        }
        cache = view.findViewById<AppCompatCheckBox>(R.id.cache).apply {
            setOnCheckedChangeListener(this@ResetDialogPreferenceFragmentCompat)
        }
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
            resetActions.add(RESET_INSTANCES)
        }
        if (forms!!.isChecked) {
            resetActions.add(RESET_FORMS)
        }
        if (layers!!.isChecked) {
            resetActions.add(RESET_LAYERS)
        }
        if (cache!!.isChecked) {
            resetActions.add(RESET_CACHE)
        }
        if (resetActions.isNotEmpty()) {
            showIfNotShowing(
                ResetProgressDialog::class.java,
                (_context as CollectAbstractActivity).supportFragmentManager
            )

            scheduler.immediate(
                {
                    return@immediate projectResetter.reset(resetActions)
                },
                { failedResetActions: List<Int> ->
                    dismissDialog(
                        ResetProgressDialog::class.java,
                        (_context as CollectAbstractActivity).supportFragmentManager
                    )
                    handleResult(resetActions, failedResetActions)
                }
            )
        }
    }

    private fun handleResult(resetActions: List<Int>, failedResetActions: List<Int>) {
        val resultMessage = StringBuilder()
        for (action in resetActions) {
            when (action) {
                RESET_PREFERENCES -> resultMessage.append(
                    _context.getLocalizedString(
                        R.string.reset_settings_result,
                        if (failedResetActions.contains(action)) {
                            R.string.error_occured
                        } else {
                            R.string.success
                        }
                    )
                )
                RESET_INSTANCES -> resultMessage.append(
                    _context.getLocalizedString(
                        R.string.reset_saved_forms_result,
                        if (failedResetActions.contains(action)) {
                            R.string.error_occured
                        } else {
                            R.string.success
                        }
                    )
                )
                RESET_FORMS -> resultMessage.append(
                    _context.getLocalizedString(
                        R.string.reset_blank_forms_result,
                        if (failedResetActions.contains(action)) {
                            R.string.error_occured
                        } else {
                            R.string.success
                        }
                    )
                )
                RESET_CACHE -> resultMessage.append(
                    _context.getLocalizedString(
                        R.string.reset_cache_result,
                        if (failedResetActions.contains(action)) {
                            R.string.error_occured
                        } else {
                            R.string.success
                        }
                    )
                )
                RESET_LAYERS -> resultMessage.append(
                    _context.getLocalizedString(
                        R.string.reset_layers_result,
                        if (failedResetActions.contains(action)) {
                            R.string.error_occured
                        } else {
                            R.string.success
                        }
                    )
                )
            }
            if (resetActions.indexOf(action) < resetActions.size - 1) {
                resultMessage.append("\n\n")
            }
        }
        if (!(_context as CollectAbstractActivity).isInstanceStateSaved) {
            (_context as CollectAbstractActivity).runOnUiThread {
                if (resetActions.contains(RESET_PREFERENCES)) {
                    (_context as CollectAbstractActivity).recreate()
                }
                val resetSettingsResultDialog =
                    ResetSettingsResultDialog.newInstance(resultMessage.toString())
                try {
                    resetSettingsResultDialog.show(
                        (_context as CollectAbstractActivity).supportFragmentManager,
                        ResetSettingsResultDialog.RESET_SETTINGS_RESULT_DIALOG_TAG
                    )
                } catch (e: ClassCastException) {
                    Timber.i(e)
                }
            }
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        adjustResetButtonAccessibility()
    }

    private fun adjustResetButtonAccessibility() {
        if (preferences!!.isChecked ||
            instances!!.isChecked ||
            forms!!.isChecked ||
            layers!!.isChecked ||
            cache!!.isChecked
        ) {
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor((dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).currentTextColor)
        } else {
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                getPartiallyTransparentColor(
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).currentTextColor
                )
            )
        }
    }

    private fun getPartiallyTransparentColor(color: Int): Int =
        Color.argb(150, Color.red(color), Color.green(color), Color.blue(color))

    companion object {
        fun newInstance(key: String): ResetDialogPreferenceFragmentCompat {
            val fragment = ResetDialogPreferenceFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }
}
