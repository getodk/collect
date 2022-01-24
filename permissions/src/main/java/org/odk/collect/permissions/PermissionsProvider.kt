package org.odk.collect.permissions

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import timber.log.Timber

/**
 * PermissionsProvider allows all permission related messages and checks to be encapsulated in one
 * area so that classes don't have to deal with this responsibility; they just receive a callback
 * that tells them if they have been granted the permission they requested.
 */
open class PermissionsProvider(private val permissionsChecker: PermissionsChecker) {
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

    open fun requestReadStoragePermission(activity: Activity, action: PermissionListener) {
        requestPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    action.granted()
                }

                override fun denied() {
                    showAdditionalExplanation(
                        activity, R.string.storage_runtime_permission_denied_title,
                        R.string.storage_runtime_permission_denied_desc, R.drawable.sd, action
                    )
                }
            },
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

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

    fun requestLocationPermissions(activity: Activity, action: PermissionListener) {
        requestPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    action.granted()
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

    /**
     * Request location permissions and make sure Location is enabled at a system level. If the
     * latter is not true, show a dialog prompting the user to do so rather than
     * [PermissionListener.granted].
     */
    fun requestEnabledLocationPermissions(activity: Activity, action: PermissionListener) {
        requestLocationPermissions(
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
                            ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                            .create()
                            .show()
                    }
                }

                override fun denied() {
                    action.denied()
                }
            }
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

    protected open fun requestPermissions(
        activity: Activity,
        listener: PermissionListener,
        vararg permissions: String
    ) {
        var builder: DexterBuilder? = null
        if (permissions.size == 1) {
            builder = createSinglePermissionRequest(activity, permissions[0], listener)
        } else if (permissions.size > 1) {
            builder = createMultiplePermissionsRequest(activity, listener, *permissions)
        }
        builder?.withErrorListener { error: DexterError -> Timber.i(error.name) }?.check()
    }

    private fun createSinglePermissionRequest(
        activity: Activity,
        permission: String,
        listener: PermissionListener
    ): DexterBuilder {
        return Dexter.withContext(activity)
            .withPermission(permission)
            .withListener(object : com.karumi.dexter.listener.single.PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    listener.granted()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    listener.denied()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
    }

    private fun createMultiplePermissionsRequest(
        activity: Activity,
        listener: PermissionListener,
        vararg permissions: String
    ): DexterBuilder {
        return Dexter.withContext(activity)
            .withPermissions(*permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        listener.granted()
                    } else {
                        listener.denied()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
    }

    protected open fun showAdditionalExplanation(
        activity: Activity,
        title: Int,
        message: Int,
        drawable: Int,
        action: PermissionListener
    ) {
        action.denied()
        val args = Bundle()
        args.putInt(PermissionDeniedDialog.TITLE, title)
        args.putInt(PermissionDeniedDialog.MESSAGE, message)
        args.putInt(PermissionDeniedDialog.ICON, drawable)
        showIfNotShowing(
            PermissionDeniedDialog::class.java,
            args,
            (activity as AppCompatActivity).supportFragmentManager
        )
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
            requestReadStoragePermission(
                activity,
                object : PermissionListener {
                    override fun granted() {
                        listener.granted()
                    }

                    override fun denied() {
                        listener.denied()
                    }
                }
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
