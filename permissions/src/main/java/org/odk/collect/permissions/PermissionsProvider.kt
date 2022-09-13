package org.odk.collect.permissions

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.core.location.LocationManagerCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * PermissionsProvider allows all permission related messages and checks to be encapsulated in one
 * area so that classes don't have to deal with this responsibility; they just receive a callback
 * that tells them if they have been granted the permission they requested.
 */
open class PermissionsProvider internal constructor(
    private val permissionsChecker: PermissionsChecker,
    private val requestPermissionsApi: RequestPermissionsAPI
) {

    /**
     * Public facing constructor that doesn't expose [RequestPermissionsAPI]
     */
    constructor(permissionsChecker: PermissionsChecker) : this(
        permissionsChecker,
        DexterRequestPermissionsAPI
    )

    val isCameraPermissionGranted: Boolean
        get() = permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)

    fun areLocationPermissionsGranted(): Boolean {
        return permissionsChecker.isPermissionGranted(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun areCameraAndRecordAudioPermissionsGranted(): Boolean {
        return permissionsChecker.isPermissionGranted(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    val isGetAccountsPermissionGranted: Boolean
        get() = permissionsChecker.isPermissionGranted(Manifest.permission.GET_ACCOUNTS)

    open val isReadPhoneStatePermissionGranted: Boolean
        get() = permissionsChecker.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)

    fun requestCameraPermission(activity: Activity, action: PermissionListener) {
        requestPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    action.granted()
                }

                override fun denied() {
                    showAdditionalExplanation(
                        activity,
                        R.string.camera_runtime_permission_denied_title,
                        R.string.camera_runtime_permission_denied_desc,
                        R.drawable.ic_photo_camera,
                        action
                    )
                }
            },
            Manifest.permission.CAMERA
        )
    }

    /**
     * Request location permissions and make sure Location is enabled at a system level. If the
     * latter is not true, show a dialog prompting the user to do so rather than
     * [PermissionListener.granted].
     */
    fun requestEnabledLocationPermissions(activity: Activity, action: PermissionListener) {
        requestPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    if (isLocationEnabled(activity)) {
                        action.granted()
                    } else {
                        MaterialAlertDialogBuilder(activity)
                            .setMessage(activity.getString(R.string.gps_enable_message))
                            .setCancelable(false)
                            .setPositiveButton(
                                activity.getString(R.string.enable_gps)
                            ) { _: DialogInterface?, _: Int ->
                                activity.startActivityForResult(
                                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0
                                )
                            }
                            .setNegativeButton(
                                activity.getString(R.string.cancel)
                            ) { dialog: DialogInterface, _: Int ->
                                action.denied()
                                dialog.cancel()
                            }
                            .create()
                            .show()
                    }
                }

                override fun denied() {
                    showAdditionalExplanation(
                        activity,
                        R.string.location_runtime_permissions_denied_title,
                        R.string.location_runtime_permissions_denied_desc,
                        R.drawable.ic_room_black_24dp,
                        action
                    )
                }
            },
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    open fun requestRecordAudioPermission(activity: Activity, action: PermissionListener) {
        requestPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    action.granted()
                }

                override fun denied() {
                    showAdditionalExplanation(
                        activity,
                        R.string.record_audio_runtime_permission_denied_title,
                        R.string.record_audio_runtime_permission_denied_desc,
                        R.drawable.ic_mic,
                        action
                    )
                }
            },
            Manifest.permission.RECORD_AUDIO
        )
    }

    fun requestCameraAndRecordAudioPermissions(activity: Activity, action: PermissionListener) {
        requestPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    action.granted()
                }

                override fun denied() {
                    showAdditionalExplanation(
                        activity,
                        R.string.camera_runtime_permission_denied_title,
                        R.string.camera_runtime_permission_denied_desc,
                        R.drawable.ic_photo_camera,
                        action
                    )
                }
            },
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        )
    }

    fun requestGetAccountsPermission(activity: Activity, action: PermissionListener) {
        requestPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    action.granted()
                }

                override fun denied() {
                    showAdditionalExplanation(
                        activity,
                        R.string.get_accounts_runtime_permission_denied_title,
                        R.string.get_accounts_runtime_permission_denied_desc,
                        R.drawable.ic_get_accounts,
                        action
                    )
                }
            },
            Manifest.permission.GET_ACCOUNTS
        )
    }

    open fun requestReadPhoneStatePermission(
        activity: Activity,
        displayPermissionDeniedDialog: Boolean,
        action: PermissionListener
    ) {
        requestPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    action.granted()
                }

                override fun denied() {
                    if (displayPermissionDeniedDialog) {
                        showAdditionalExplanation(
                            activity,
                            R.string.read_phone_state_runtime_permission_denied_title,
                            R.string.read_phone_state_runtime_permission_denied_desc,
                            R.drawable.ic_phone,
                            action
                        )
                    } else {
                        action.denied()
                    }
                }
            },
            Manifest.permission.READ_PHONE_STATE
        )
    }

    open fun requestPermissions(
        activity: Activity,
        listener: PermissionListener,
        vararg permissions: String
    ) {
        val safePermissionsListener = object : PermissionListener {
            override fun granted() {
                if (!activity.isFinishing) {
                    listener.granted()
                }
            }

            override fun denied() {
                if (!activity.isFinishing) {
                    listener.denied()
                }
            }
        }

        requestPermissionsApi.requestPermissions(activity, safePermissionsListener, *permissions)
    }

    protected open fun showAdditionalExplanation(
        activity: Activity,
        title: Int,
        message: Int,
        drawable: Int,
        action: PermissionListener
    ) {
        action.denied()

        MaterialAlertDialogBuilder(activity)
            .setIcon(drawable)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ ->
                action.additionalExplanationClosed()
            }
            .setNeutralButton(R.string.open_settings) { _, _ ->
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                    activity.startActivity(this)
                }
            }
            .create()
            .show()
    }

    fun requestReadUriPermission(
        activity: Activity,
        uri: Uri,
        contentResolver: ContentResolver,
        listener: PermissionListener
    ) {
        try {
            contentResolver.query(uri, null, null, null, null)
                .use { listener.granted() }
        } catch (e: SecurityException) {
            requestPermissions(
                activity,
                object : PermissionListener {
                    override fun granted() {
                        listener.granted()
                    }

                    override fun denied() {
                        showAdditionalExplanation(
                            activity, R.string.storage_runtime_permission_denied_title,
                            R.string.storage_runtime_permission_denied_desc, R.drawable.sd,
                            listener
                        )
                    }
                },
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } catch (e: Exception) {
            listener.denied()
        } catch (e: Error) {
            listener.denied()
        }
    }

    private fun isLocationEnabled(activity: Activity): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}
