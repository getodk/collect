package org.odk.collect.permissions

import android.app.Activity
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import timber.log.Timber

internal interface RequestPermissionsAPI {
    fun requestPermissions(
        activity: Activity,
        listener: PermissionListener,
        vararg permissions: String
    )
}

internal object DexterRequestPermissionsAPI : RequestPermissionsAPI {

    override fun requestPermissions(
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
}
