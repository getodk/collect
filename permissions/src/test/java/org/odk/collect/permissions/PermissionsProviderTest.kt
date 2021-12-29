package org.odk.collect.permissions

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PermissionsProviderTest {
    private var permissionsChecker = mock<PermissionsChecker>()
    private var activity = mock<Activity>()
    private var uri = mock<Uri>()
    private var contentResolver = mock<ContentResolver>()
    private var permissionListener = mock<PermissionListener>()
    private lateinit var permissionsProvider: PermissionsProvider

    @Before
    fun setup() {
        permissionsProvider = spy(PermissionsProvider(permissionsChecker))
    }

    @Test
    fun `When camera permission granted should isCameraPermissionGranted() return true`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)).thenReturn(true)

        assertThat(permissionsProvider.isCameraPermissionGranted, `is`(true))
    }

    @Test
    fun `When camera permission not granted should isCameraPermissionGranted() return false`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)).thenReturn(
            false
        )

        assertThat(permissionsProvider.isCameraPermissionGranted, `is`(false))
    }

    @Test
    fun `When location permissions granted should areLocationPermissionsGranted() return true`() {
        whenever(
            permissionsChecker.isPermissionGranted(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ).thenReturn(true)

        assertThat(permissionsProvider.areLocationPermissionsGranted(), `is`(true))
    }

    @Test
    fun `When location permissions not granted should areLocationPermissionsGranted() return false`() {
        whenever(
            permissionsChecker.isPermissionGranted(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ).thenReturn(false)

        assertThat(permissionsProvider.areLocationPermissionsGranted(), `is`(false))
    }

    @Test
    fun `When camera and audio permissions granted should areCameraAndRecordAudioPermissionsGranted() return true`() {
        whenever(
            permissionsChecker.isPermissionGranted(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        ).thenReturn(true)

        assertThat(permissionsProvider.areCameraAndRecordAudioPermissionsGranted(), `is`(true))
    }

    @Test
    fun `When camera and audio permissions not granted should areCameraAndRecordAudioPermissionsGranted() return false`() {
        whenever(
            permissionsChecker.isPermissionGranted(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        ).thenReturn(false)

        assertThat(permissionsProvider.areCameraAndRecordAudioPermissionsGranted(), `is`(false))
    }

    @Test
    fun `When get accounts permission granted should isGetAccountsPermissionGranted() return true`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.GET_ACCOUNTS)).thenReturn(
            true
        )

        assertThat(permissionsProvider.isGetAccountsPermissionGranted, `is`(true))
    }

    @Test
    fun `When get accounts permission not granted should isGetAccountsPermissionGranted() return false`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.GET_ACCOUNTS)).thenReturn(
            false
        )

        assertThat(permissionsProvider.isGetAccountsPermissionGranted, `is`(false))
    }

    @Test
    fun `When read phone state permission granted should isReadPhoneStatePermissionGranted() return true`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)).thenReturn(
            true
        )

        assertThat(permissionsProvider.isReadPhoneStatePermissionGranted, `is`(true))
    }

    @Test
    fun `When read phone state permission not granted should isReadPhoneStatePermissionGranted() return false`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)).thenReturn(
            false
        )

        assertThat(permissionsProvider.isReadPhoneStatePermissionGranted, `is`(false))
    }

    @Test
    fun `When request read uri permission granted should granted be called`() {
        permissionsProvider.requestReadUriPermission(
            activity,
            uri,
            contentResolver,
            permissionListener
        )
        verify(permissionListener).granted()
    }

    @Test
    fun `When request read uri permission not granted should user be asked to grant read storage permission`() {
        whenever(contentResolver.query(uri, null, null, null, null)).thenThrow(
            SecurityException::class.java
        )
        doNothing().whenever(permissionsProvider).requestReadStoragePermission(any(), any())

        permissionsProvider.requestReadUriPermission(
            activity,
            uri,
            contentResolver,
            permissionListener
        )
        verify(permissionsProvider).requestReadStoragePermission(any(), any())
    }

    @Test
    fun `When request read uri permission throws any exception should denied be called`() {
        whenever(contentResolver.query(uri, null, null, null, null)).thenThrow(
            RuntimeException::class.java
        )
        permissionsProvider.requestReadUriPermission(
            activity,
            uri,
            contentResolver,
            permissionListener
        )
        verify(permissionListener).denied()
    }
}
