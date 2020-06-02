package org.odk.collect.android.location.client;

class TestClientListener implements LocationClient.LocationClientListener {

    private boolean wasStartCalled;
    private boolean wasStartFailureCalled;
    private int onClientStopCount;

    void reset() {
        wasStartCalled = false;
        wasStartFailureCalled = false;
        onClientStopCount = 0;
    }

    boolean wasStartCalled() {
        return wasStartCalled;
    }

    boolean wasStartFailureCalled() {
        return wasStartFailureCalled;
    }

    boolean wasStopCalled() {
        return onClientStopCount > 0;
    }

    int getOnClientStopCount() {
        return onClientStopCount;
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
        onClientStopCount++;
    }
}