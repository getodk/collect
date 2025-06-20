package org.odk.collect.android.support

import android.app.Application
import android.content.Context
import android.content.RestrictionsManager
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.work.WorkManager
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.androidshared.system.BroadcastReceiverRegister
import org.odk.collect.async.Scheduler
import org.odk.collect.async.network.NetworkStateProvider
import org.odk.collect.openrosa.http.OpenRosaHttpInterface
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.testshared.FakeBarcodeScannerViewFactory
import org.odk.collect.testshared.FakeBroadcastReceiverRegister
import org.odk.collect.utilities.UserAgentProvider

open class TestDependencies @JvmOverloads constructor(
    private val useRealServer: Boolean = false
) : AppDependencyModule() {
    @JvmField val server: StubOpenRosaServer = StubOpenRosaServer()

    @JvmField val storagePathProvider: StoragePathProvider = StoragePathProvider()

    val networkStateProvider: FakeNetworkStateProvider = FakeNetworkStateProvider()
    val scheduler: TestScheduler = TestScheduler(networkStateProvider)
    val fakeBarcodeScannerViewFactory = FakeBarcodeScannerViewFactory()
    val broadcastReceiverRegister: FakeBroadcastReceiverRegister = FakeBroadcastReceiverRegister()
    val restrictionsManager: RestrictionsManager = mock<RestrictionsManager>().apply {
        whenever(applicationRestrictions).thenReturn(Bundle())
    }

    override fun provideHttpInterface(
        mimeTypeMap: MimeTypeMap,
        userAgentProvider: UserAgentProvider,
        application: Application,
        versionInformation: VersionInformation
    ): OpenRosaHttpInterface {
        return if (useRealServer) {
            super.provideHttpInterface(
                mimeTypeMap,
                userAgentProvider,
                application,
                versionInformation
            )
        } else {
            server
        }
    }

    override fun providesScheduler(workManager: WorkManager): Scheduler {
        return scheduler
    }

    override fun providesBarcodeScannerViewFactory(settingsProvider: SettingsProvider): BarcodeScannerViewContainer.Factory {
        return fakeBarcodeScannerViewFactory
    }

    override fun providesNetworkStateProvider(context: Context): NetworkStateProvider {
        return networkStateProvider
    }

    override fun providesBroadcastReceiverRegister(context: Context): BroadcastReceiverRegister {
        return broadcastReceiverRegister
    }

    override fun providesRestrictionsManager(context: Context): RestrictionsManager {
        return restrictionsManager
    }
}
