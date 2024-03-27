package org.odk.collect.android.instancemanagement

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.odk.collect.android.R
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.forms.instances.Instance
import org.odk.collect.material.ErrorsPill
import org.odk.collect.strings.R.string
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InstanceListItemView {

    private const val DISABLED_ALPHA = 0.38f

    @JvmStatic
    @Deprecated("This should eventually be replaced by a ViewHolder or View implementation")
    fun setInstance(view: View, instance: Instance, shouldCheckDisabled: Boolean) {
        val context = view.context

        val imageView = view.findViewById<ImageView>(R.id.image)
        setImageFromStatus(imageView, instance)
        setUpSubtext(view, instance, context)

        val pill = view.findViewById<ErrorsPill>(R.id.chip)
        if (pill != null) {
            when (instance.status) {
                Instance.STATUS_INVALID, Instance.STATUS_INCOMPLETE -> pill.errors = true
                Instance.STATUS_VALID -> pill.errors = false
                else -> pill.visibility = View.GONE
            }
        }

        if (shouldCheckDisabled) {
            var formExists = false
            var isFormEncrypted = false
            val formId = instance.formId
            val formVersion = instance.formVersion
            val form = FormsRepositoryProvider(context.applicationContext).get()
                .getLatestByFormIdAndVersion(formId, formVersion)

            if (form != null) {
                val base64RSAPublicKey = form.basE64RSAPublicKey
                formExists = true
                isFormEncrypted = base64RSAPublicKey != null
            }

            val date = instance.deletedDate
            if (date != null || !formExists || isFormEncrypted) {
                val disabledMessage = if (date != null) {
                    try {
                        val deletedTime: String = context.getString(string.deleted_on_date_at_time)
                        SimpleDateFormat(deletedTime, Locale.getDefault()).format(Date(date))
                    } catch (e: IllegalArgumentException) {
                        Timber.e(e)
                        context.getString(string.submission_deleted)
                    }
                } else if (!formExists) {
                    context.getString(string.deleted_form)
                } else {
                    context.getString(string.encrypted_form)
                }

                setDisabled(view, disabledMessage)
            } else {
                setEnabled(view)
            }
        }
    }

    private fun setEnabled(view: View) {
        val formTitle = view.findViewById<TextView>(R.id.form_title)
        val formSubtitle = view.findViewById<TextView>(R.id.form_subtitle)
        val disabledCause = view.findViewById<TextView>(R.id.form_subtitle2)
        val imageView = view.findViewById<ImageView>(R.id.image)
        view.isEnabled = true
        disabledCause.visibility = View.GONE
        formTitle.alpha = 1f
        formSubtitle.alpha = 1f
        disabledCause.alpha = 1f
        imageView.alpha = 1f
    }

    private fun setDisabled(view: View, disabledMessage: String) {
        val formTitle = view.findViewById<TextView>(R.id.form_title)
        val formSubtitle = view.findViewById<TextView>(R.id.form_subtitle)
        val disabledCause = view.findViewById<TextView>(R.id.form_subtitle2)
        val imageView = view.findViewById<ImageView>(R.id.image)
        view.isEnabled = false
        disabledCause.visibility = View.VISIBLE
        disabledCause.text = disabledMessage

        // Material design "disabled" opacity is 38%.
        formTitle.alpha = DISABLED_ALPHA
        formSubtitle.alpha = DISABLED_ALPHA
        disabledCause.alpha = DISABLED_ALPHA
        imageView.alpha = DISABLED_ALPHA
    }

    private fun setUpSubtext(view: View, instance: Instance, context: Context) {
        val subtext = instance.getStatusDescription(context.resources)
        val formSubtitle = view.findViewById<TextView>(R.id.form_subtitle)
        formSubtitle.text = subtext
    }

    private fun setImageFromStatus(imageView: ImageView, instance: Instance) {
        val imageResourceId = instance.getIcon()
        imageView.setImageResource(imageResourceId)
        imageView.tag = imageResourceId
    }
}
