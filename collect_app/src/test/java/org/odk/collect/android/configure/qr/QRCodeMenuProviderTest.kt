package org.odk.collect.android.configure.qr

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.TestSettingsProvider.getSettingsProvider
import org.odk.collect.android.utilities.FileProvider
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.androidshared.system.IntentLauncherImpl
import org.odk.collect.testshared.ErrorIntentLauncher
import org.odk.collect.testshared.FakeScheduler
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class QRCodeMenuProviderTest {
    private lateinit var activity: FragmentActivity
    private lateinit var intentLauncher: IntentLauncher
    private lateinit var menuProvider: QRCodeMenuProvider

    private val qrCodeGenerator = mock<QRCodeGenerator>()
    private val appConfigurationGenerator = mock<AppConfigurationGenerator>()
    private val fileProvider = mock<FileProvider>()
    private val fakeScheduler = FakeScheduler()

    @Before
    fun setup() {
        activity = Robolectric.setupActivity(FragmentActivity::class.java)
        intentLauncher = IntentLauncherImpl
        setupMenuProvider()
    }

    private fun setupMenuProvider() {
        menuProvider = QRCodeMenuProvider(
            activity,
            intentLauncher,
            qrCodeGenerator,
            appConfigurationGenerator,
            fileProvider,
            getSettingsProvider(),
            fakeScheduler
        )
    }

    @Test
    fun clickingOnImportQRCode_startsExternalImagePickerIntent() {
        menuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_item_scan_sd_card))
        val intentForResult = Shadows.shadowOf(activity).nextStartedActivityForResult

        assertThat(intentForResult, notNullValue())
        assertThat(
            intentForResult.requestCode,
            equalTo(QRCodeMenuProvider.SELECT_PHOTO)
        )
        assertThat(
            intentForResult.intent.action,
            equalTo(Intent.ACTION_GET_CONTENT)
        )
        assertThat(intentForResult.intent.type, equalTo("image/*"))
    }

    @Test
    fun clickingOnImportQRCode_whenPickerActivityNotAvailable_showsToast() {
        intentLauncher = ErrorIntentLauncher()
        setupMenuProvider()
        menuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_item_scan_sd_card))

        assertThat(
            Shadows.shadowOf(activity).nextStartedActivityForResult,
            nullValue()
        )
        assertThat(ShadowToast.getLatestToast(), notNullValue())
    }

    @Test
    fun clickingOnShare_beforeQRCodeIsGenerated_doesNothing() {
        whenever(qrCodeGenerator.generateQRCode(any(), any())).thenReturn("qr.png")
        whenever(fileProvider.getURIForFile("qr.png")).thenReturn(Uri.parse("uri"))
        menuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_item_share))

        assertThat(Shadows.shadowOf(activity).nextStartedActivity, nullValue())
    }

    @Test
    fun clickingOnShare_whenQRCodeIsGenerated_startsShareIntent() {
        whenever(qrCodeGenerator.generateQRCode(any(), any())).thenReturn("qr.png")
        whenever(fileProvider.getURIForFile("qr.png")).thenReturn(Uri.parse("uri"))
        fakeScheduler.runBackground()
        menuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_item_share))
        val intent = Shadows.shadowOf(activity).nextStartedActivity

        assertThat(intent, notNullValue())
        assertThat(intent.action, equalTo(Intent.ACTION_SEND))
        assertThat(intent.type, equalTo("image/*"))
        assertThat(intent.extras!!.getParcelable(Intent.EXTRA_STREAM), equalTo(Uri.parse("uri")))
    }
}
