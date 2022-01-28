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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.odk.collect.android.R
import org.odk.collect.android.activities.CollectAbstractActivity
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.utilities.ProjectResetter
import org.odk.collect.android.utilities.ProjectResetter.ResetAction.RESET_PREFERENCES
import org.odk.collect.androidshared.ui.DialogFragmentUtils.dismissDialog
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import timber.log.Timber
import javax.inject.Inject

class ResetDialogPreferenceFragmentCompat :
    PreferenceDialogFragmentCompat(),
    CompoundButton.OnCheckedChangeListener {

    @JvmField
    @Inject
    var projectResetter: ProjectResetter? = null

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var _preferences: AppCompatCheckBox? = null
    private val preferences: AppCompatCheckBox
        get() = _preferences!!
    private var _instances: AppCompatCheckBox? = null
    private val instances: AppCompatCheckBox
        get() = _instances!!
    private var _forms: AppCompatCheckBox? = null
    private val forms: AppCompatCheckBox
        get() = _forms!!
    private var _layers: AppCompatCheckBox? = null
    private val layers: AppCompatCheckBox
        get() = _layers!!
    private var _cache: AppCompatCheckBox? = null
    private val cache: AppCompatCheckBox
        get() = _cache!!

    private lateinit var _context: Context

    override fun onAttach(context: Context) {
        _context = context
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    public override fun onBindDialogView(view: View) {
        _preferences = view.findViewById(R.id.preferences)
        _instances = view.findViewById(R.id.instances)
        _forms = view.findViewById(R.id.forms)
        _layers = view.findViewById(R.id.layers)
        _cache = view.findViewById(R.id.cache)
        preferences.setOnCheckedChangeListener(this)
        instances.setOnCheckedChangeListener(this)
        forms.setOnCheckedChangeListener(this)
        layers.setOnCheckedChangeListener(this)
        cache.setOnCheckedChangeListener(this)
        super.onBindDialogView(view)
    }

    override fun onStart() {
        super.onStart()
        adjustResetButtonAccessibility()
    }

    override fun onDetach() {
        _preferences = null
        _instances = null
        _forms = null
        _layers = null
        _cache = null
        super.onDetach()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            resetSelected()
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {}

    private fun resetSelected() {
        val resetActions: MutableList<Int> = ArrayList()
        if (preferences.isChecked) {
            resetActions.add(RESET_PREFERENCES)
        }
        if (instances.isChecked) {
            resetActions.add(ProjectResetter.ResetAction.RESET_INSTANCES)
        }
        if (forms.isChecked) {
            resetActions.add(ProjectResetter.ResetAction.RESET_FORMS)
        }
        if (layers.isChecked) {
            resetActions.add(ProjectResetter.ResetAction.RESET_LAYERS)
        }
        if (cache.isChecked) {
            resetActions.add(ProjectResetter.ResetAction.RESET_CACHE)
        }
        if (resetActions.isNotEmpty()) {
            showIfNotShowing(
                ResetProgressDialog::class.java,
                (_context as CollectAbstractActivity).supportFragmentManager
            )

            scope.launch(Dispatchers.IO) {

                val failedResetActions = projectResetter!!.reset(resetActions)

                withContext(Dispatchers.Main) {
                    dismissDialog(
                        ResetProgressDialog::class.java,
                        (_context as CollectAbstractActivity).supportFragmentManager
                    )
                    handleResult(resetActions, failedResetActions)
                }
            }
        }
    }

    private fun handleResult(resetActions: List<Int>, failedResetActions: List<Int>) {
        val resultMessage = StringBuilder()
        for (action in resetActions) {
            when (action) {
                RESET_PREFERENCES -> if (failedResetActions.contains(action)) {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_settings_result),
                            _context.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_settings_result),
                            _context.getString(R.string.success)
                        )
                    )
                }
                ProjectResetter.ResetAction.RESET_INSTANCES -> if (failedResetActions.contains(
                        action
                    )
                ) {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_saved_forms_result),
                            _context.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_saved_forms_result),
                            _context.getString(R.string.success)
                        )
                    )
                }
                ProjectResetter.ResetAction.RESET_FORMS -> if (failedResetActions.contains(action)) {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_blank_forms_result),
                            _context.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_blank_forms_result),
                            _context.getString(R.string.success)
                        )
                    )
                }
                ProjectResetter.ResetAction.RESET_CACHE -> if (failedResetActions.contains(action)) {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_cache_result),
                            _context.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_cache_result),
                            _context.getString(R.string.success)
                        )
                    )
                }
                ProjectResetter.ResetAction.RESET_LAYERS -> if (failedResetActions.contains(action)) {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_layers_result),
                            _context.getString(R.string.error_occured)
                        )
                    )
                } else {
                    resultMessage.append(
                        String.format(
                            _context.getString(R.string.reset_layers_result),
                            _context.getString(R.string.success)
                        )
                    )
                }
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
        if (preferences.isChecked ||
            instances.isChecked ||
            forms.isChecked ||
            layers.isChecked ||
            cache.isChecked
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
        fun newInstance(key: String?): ResetDialogPreferenceFragmentCompat {
            val fragment = ResetDialogPreferenceFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }
}
