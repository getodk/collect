package org.odk.collect.material

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import org.odk.collect.androidshared.ui.EdgeToEdge.handleEdgeToEdge

/**
 * Provides an implementation of Material's "Full Screen Dialog"
 * (https://material.io/components/dialogs/#full-screen-dialog) as no implementation currently
 * exists in the Material Components framework
 */
abstract class MaterialFullScreenDialogFragment : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog_FullScreen)
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as ComponentDialog?
        dialog?.window?.apply {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            setLayout(width, height)

            if (shouldShowSoftKeyboard()) {
                // Make sure soft keyboard shows for focused field - annoyingly needed
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }

            setCancelable(false)
            dialog.onBackPressedDispatcher.addCallback(this@MaterialFullScreenDialogFragment, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })

            handleEdgeToEdge(requireContext())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getToolbar()?.apply {
            setNavigationOnClickListener { _: View? ->
                onCloseClicked()
            }
        }
    }

    protected abstract fun onCloseClicked()

    protected abstract fun onBackPressed()

    protected abstract fun getToolbar(): Toolbar?

    protected open fun shouldShowSoftKeyboard(): Boolean {
        return false
    }
}
