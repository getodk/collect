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
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class PermissionsProviderTest {

    private var permissionsChecker = mock<PermissionsChecker>()
    private var activity = mock<Activity>()
    private var uri = mock<Uri>()
    private var contentResolver = mock<ContentResolver>()
    private var permissionListener = mock<PermissionListener>()
    private val permissionsApi = mock<RequestPermissionsAPI> {
        on { requestPermissions(any(), any(), any()) } doAnswer {
            (it.getArgument(1) as PermissionListener).granted()
        }
    }
    private val permissionsDialogCreator = mock<PermissionsDialogCreator>()
    private val locationAccessibilityChecker = mock<LocationAccessibilityChecker>()

    private lateinit var permissionsProvider: PermissionsProvider

    @Before
    fun setup() {
        permissionsProvider = PermissionsProvider(permissionsChecker, permissionsApi, permissionsDialogCreator, locationAccessibilityChecker)
    }

    @Test
    fun `When camera permission granted should isCameraPermissionGranted() return true`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)).thenReturn(true)

        assertThat(permissionsProvider.isCameraPermissionGranted, `is`(true))
    }

    @Test
    fun `When camera permission not granted should isCameraPermissionGranted() return false`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA))
            .thenReturn(false)

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

    @Test
    fun `granted listener is not called when Activity is finishing`() {
        whenever(permissionsApi.requestPermissions(any(), any(), any())).doAnswer {
            (it.getArgument(1) as PermissionListener).granted()
        }

        whenever(activity.isFinishing).doReturn(true)

        permissionsProvider.requestPermissions(
            activity,
            permissionListener,
            Manifest.permission.READ_PHONE_STATE
        )

        verifyNoInteractions(permissionListener)
    }

    @Test
    fun `denied listener is not called when Activity is finishing`() {
        whenever(permissionsApi.requestPermissions(any(), any(), any())).doAnswer {
            (it.getArgument(1) as PermissionListener).denied()
        }

        whenever(activity.isFinishing).doReturn(true)

        permissionsProvider.requestPermissions(
            activity,
            permissionListener,
            Manifest.permission.READ_PHONE_STATE
        )

        verifyNoInteractions(permissionListener)
    }
}
