package org.odk.collect.android.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.webkit.MimeTypeMap;

import androidx.work.WorkManager;

import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.version.VersionInformation;
import org.odk.collect.android.views.BarcodeViewDecoder;
import org.odk.collect.androidshared.system.BroadcastReceiverRegister;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.network.NetworkStateProvider;
import org.odk.collect.openrosa.http.OpenRosaHttpInterface;
import org.odk.collect.testshared.FakeBroadcastReceiverRegister;
import org.odk.collect.utilities.UserAgentProvider;

public class TestDependencies extends AppDependencyModule {

    public final StubOpenRosaServer server = new StubOpenRosaServer();
    public final FakeNetworkStateProvider networkStateProvider = new FakeNetworkStateProvider();
    public final TestScheduler scheduler = new TestScheduler(networkStateProvider);
    public final StoragePathProvider storagePathProvider = new StoragePathProvider();
    public final StubBarcodeViewDecoder stubBarcodeViewDecoder = new StubBarcodeViewDecoder();
    public final FakeBroadcastReceiverRegister broadcastReceiverRegister = new FakeBroadcastReceiverRegister();
    public final RestrictionsManager restrictionsManager = mock(RestrictionsManager.class);
    private final boolean useRealServer;

    public TestDependencies() {
        this(false);
    }

    public TestDependencies(boolean useRealServer) {
        this.useRealServer = useRealServer;
        when(restrictionsManager.getApplicationRestrictions()).thenReturn(new Bundle());
    }

    @Override
    public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider, Application application, VersionInformation versionInformation) {
        if (useRealServer) {
            return super.provideHttpInterface(mimeTypeMap, userAgentProvider, application, versionInformation);
        } else {
            return server;
        }
    }

    @Override
    public Scheduler providesScheduler(WorkManager workManager) {
        return scheduler;
    }

    @Override
    public BarcodeViewDecoder providesBarcodeViewDecoder() {
        return stubBarcodeViewDecoder;
    }

    @Override
    public NetworkStateProvider providesNetworkStateProvider(Context context) {
        return networkStateProvider;
    }

    @Override
    public BroadcastReceiverRegister providesBroadcastReceiverRegister(Context context) {
        return broadcastReceiverRegister;
    }

    @Override
    public RestrictionsManager providesRestrictionsManager(Context context) {
        return restrictionsManager;
    }
}
