package org.odk.collect.android.location.client;

import org.odk.collect.location.LocationClient;

class TestClientListener implements LocationClient.LocationClientListener {

    private boolean wasStartCalled;
    private boolean wasStartFailureCalled;
    private boolean wasStopCalled;

    void reset() {
        wasStartCalled = false;
        wasStartFailureCalled = false;
        wasStopCalled = false;
    }

    boolean wasStartCalled() {
        return wasStartCalled;
    }

    boolean wasStartFailureCalled() {
        return wasStartFailureCalled;
    }

    boolean wasStopCalled() {
        return wasStopCalled;
    }

    @Override
    public void onClientStart() {
        wasStartCalled = true;
    }

    @Override
    public void onClientStartFailure() {
        wasStartFailureCalled = true;
    }

    @Override
    public void onClientStop() {
        wasStopCalled = true;
    }
}
