package org.odk.collect.android.location;

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
    public void onStart() {
        wasStartCalled = true;
    }

    @Override
    public void onStartFailure() {
        wasStartFailureCalled = true;
    }

    @Override
    public void onStop() {
        wasStopCalled = true;
    }
}