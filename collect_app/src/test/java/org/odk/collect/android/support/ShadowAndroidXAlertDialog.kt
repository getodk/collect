package org.odk.collect.android.support

import android.view.View
import androidx.appcompat.app.AlertDialog
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadows.ShadowDialog
import org.robolectric.util.ReflectionHelpers

@Implements(AlertDialog::class)
class ShadowAndroidXAlertDialog : ShadowDialog() {

    @RealObject
    private lateinit var realObject: AlertDialog

    fun getView(): View {
        val alertController = ReflectionHelpers.getField<Any>(realObject, "mAlert")
        return ReflectionHelpers.getField(alertController, "mView")
    }
}
