package org.odk.collect.android.utilities

import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.analytics.AnalyticsUtils
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import java.util.Locale

object InstanceAutoDeleteChecker {

    /**
     * Returns whether instances of the form specified should be auto-deleted after successful
     * update.
     *
     * If the form explicitly sets the auto-delete property, then it overrides the preference.
     */
    @JvmStatic
    fun shouldInstanceBeDeleted(
        formsRepository: FormsRepository,
        isAutoDeleteEnabledInProjectSettings: Boolean,
        instance: Instance
    ): Boolean {
        formsRepository.getLatestByFormIdAndVersion(instance.formId, instance.formVersion)?.let { form ->
            if (!form.autoDelete.isNullOrEmpty()) {
                AnalyticsUtils.logFormEvent(AnalyticsEvents.FORM_LEVEL_AUTO_DELETE, form.formId, form.displayName)
            }

            return if (isAutoDeleteEnabledInProjectSettings) {
                form.autoDelete == null || form.autoDelete.trim().lowercase(Locale.US) != "false"
            } else {
                form.autoDelete != null && form.autoDelete.trim().lowercase(Locale.US) == "true"
            }
        }

        return false
    }
}
