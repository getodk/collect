package org.odk.collect.androidshared.ui

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import timber.log.Timber

object DialogFragmentUtils {

    @JvmStatic
    fun <T : DialogFragment> showIfNotShowing(
        dialogClass: Class<T>,
        fragmentManager: FragmentManager
    ) {
        showIfNotShowing(dialogClass, null, fragmentManager)
    }

    @JvmStatic
    fun <T : DialogFragment> showIfNotShowing(
        dialogClass: Class<T>,
        args: Bundle?,
        fragmentManager: FragmentManager
    ) {
        if (fragmentManager.isDestroyed) {
            return
        }

        val fragmentFactory = fragmentManager.fragmentFactory
        val instance = fragmentFactory.instantiate(dialogClass.classLoader, dialogClass.name) as T
        instance.arguments = args
        showIfNotShowing(instance, dialogClass, fragmentManager)
    }

    @JvmStatic
    fun <T : DialogFragment> showIfNotShowing(
        newDialog: T,
        dialogClass: Class<T>,
        fragmentManager: FragmentManager
    ) {
        showIfNotShowing(newDialog, dialogClass.name, fragmentManager)
    }

    @JvmStatic
    fun <T : DialogFragment> showIfNotShowing(
        newDialog: T,
        tag: String,
        fragmentManager: FragmentManager
    ) {
        if (fragmentManager.isStateSaved) {
            return
        }
        val existingDialog = fragmentManager.findFragmentByTag(tag) as T?
        if (existingDialog == null) {
            newDialog.show(fragmentManager.beginTransaction(), tag)

            // We need to execute this transaction. Otherwise a follow up call to this method
            // could happen before the Fragment exists in the Fragment Manager and so the
            // call to findFragmentByTag would return null and result in second dialog being show.
            try {
                fragmentManager.executePendingTransactions()
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    @JvmStatic
    fun dismissDialog(dialogClazz: Class<*>, fragmentManager: FragmentManager) {
        dismissDialog(dialogClazz.name, fragmentManager)
    }

    @JvmStatic
    fun dismissDialog(tag: String, fragmentManager: FragmentManager) {
        val existingDialog = fragmentManager.findFragmentByTag(tag) as DialogFragment?
        if (existingDialog != null) {
            existingDialog.dismissAllowingStateLoss()

            // We need to execute this transaction. Otherwise a next attempt to display a dialog
            // could happen before the Fragment is dismissed in Fragment Manager and so the
            // call to findFragmentByTag would return something (not null) and as a result the
            // next dialog won't be displayed.
            try {
                fragmentManager.executePendingTransactions()
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }
}
