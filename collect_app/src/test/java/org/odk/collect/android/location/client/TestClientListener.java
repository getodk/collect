package org.odk.collect.android.location.client;

class TestClientListener implements LocationClient.LocationClientListener {

    private boolean wasStartCalled = false;
    private boolean wasStartFailureCalled = false;
    private boolean wasStopCalled = false;

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