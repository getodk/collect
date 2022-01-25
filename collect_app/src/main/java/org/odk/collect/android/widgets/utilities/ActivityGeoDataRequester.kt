package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.FormEntryPromptUtils
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.geo.Constants.EXTRA_DRAGGABLE_ONLY
import org.odk.collect.geo.Constants.EXTRA_READ_ONLY
import org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import org.odk.collect.geo.GeoPointActivity
import org.odk.collect.geo.GeoPointMapActivity
import org.odk.collect.geo.GeoPolyActivity
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider
import java.lang.Boolean.parseBoolean

class ActivityGeoDataRequester(
    private val permissionsProvider: PermissionsProvider,
    private val activity: Activity
) : GeoDataRequester {

    override fun requestGeoPoint(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: WaitingForDataRegistry
    ) {
        permissionsProvider.requestEnabledLocationPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    waitingForDataRegistry.waitForData(prompt.index)

                    val bundle = Bundle().also {
                        if (!answerText.isNullOrEmpty()) {
                            it.putDoubleArray(
                                GeoPointMapActivity.EXTRA_LOCATION,
                                GeoWidgetUtils.getLocationParamsFromStringAnswer(answerText),
                            )
                        }

                        val accuracyThreshold =
                            FormEntryPromptUtils.getBodyAttribute(prompt, "accuracyThreshold")
                        val unacceptableAccuracyThreshold =
                            FormEntryPromptUtils.getBodyAttribute(
                                prompt,
                                "unacceptableAccuracyThreshold",
                            )

                        it.putFloat(
                            GeoPointActivity.EXTRA_ACCURACY_THRESHOLD,
                            accuracyThreshold?.toFloat() ?: DEFAULT_ACCURACY_THRESHOLD,
                        )

                        it.putFloat(
                            GeoPointActivity.EXTRA_UNACCEPTABLE_ACCURACY_THRESHOLD,
                            unacceptableAccuracyThreshold?.toFloat()
                                ?: DEFAULT_UNACCEPTABLE_ACCURACY_THRESHOLD,
                        )

                        it.putBoolean(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt))
                        it.putBoolean(EXTRA_READ_ONLY, prompt.isReadOnly)
                        it.putBoolean(EXTRA_DRAGGABLE_ONLY, hasPlacementMapAppearance(prompt))
                    }

                    val intent = Intent(
                        activity,
                        if (isMapsAppearance(prompt)) GeoPointMapActivity::class.java else GeoPointActivity::class.java,
                    ).also {
                        it.putExtras(bundle)
                    }

                    activity.startActivityForResult(
                        intent,
                        ApplicationConstants.RequestCodes.LOCATION_CAPTURE,
                    )
                }

                override fun denied() {}
            },
        )
    }

    override fun requestGeoShape(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: WaitingForDataRegistry
    ) {
        permissionsProvider.requestLocationPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    waitingForDataRegistry.waitForData(prompt.index)

                    val intent = Intent(activity, GeoPolyActivity::class.java).also {
                        it.putExtra(GeoPolyActivity.ANSWER_KEY, answerText)
                        it.putExtra(
                            GeoPolyActivity.OUTPUT_MODE_KEY,
                            GeoPolyActivity.OutputMode.GEOSHAPE,
                        )
                        it.putExtra(EXTRA_READ_ONLY, prompt.isReadOnly)
                        it.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt))
                    }

                    activity.startActivityForResult(
                        intent,
                        ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE,
                    )
                }

                override fun denied() {}
            },
        )
    }

    override fun requestGeoTrace(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: WaitingForDataRegistry
    ) {
        permissionsProvider.requestLocationPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    waitingForDataRegistry.waitForData(prompt.index)

                    val intent = Intent(activity, GeoPolyActivity::class.java).also {
                        it.putExtra(GeoPolyActivity.ANSWER_KEY, answerText)
                        it.putExtra(
                            GeoPolyActivity.OUTPUT_MODE_KEY,
                            GeoPolyActivity.OutputMode.GEOTRACE,
                        )
                        it.putExtra(EXTRA_READ_ONLY, prompt.isReadOnly)
                        it.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt))
                    }

                    activity.startActivityForResult(
                        intent,
                        ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE,
                    )
                }

                override fun denied() {}
            },
        )
    }

    private fun getAllowMockAccuracy(prompt: FormEntryPrompt): Boolean {
        return parseBoolean(
            FormEntryPromptUtils.getBindAttribute(
                prompt,
                "allow-mock-accuracy"
            )
        )
    }

    private fun isMapsAppearance(prompt: FormEntryPrompt): Boolean {
        return hasMapsAppearance(prompt) || hasPlacementMapAppearance(prompt)
    }

    private fun hasMapsAppearance(prompt: FormEntryPrompt): Boolean {
        return Appearances.hasAppearance(prompt, Appearances.MAPS)
    }

    private fun hasPlacementMapAppearance(prompt: FormEntryPrompt): Boolean {
        return Appearances.hasAppearance(prompt, Appearances.PLACEMENT_MAP)
    }

    companion object {
        const val DEFAULT_ACCURACY_THRESHOLD = 5f
        const val DEFAULT_UNACCEPTABLE_ACCURACY_THRESHOLD = 100f
    }
}
