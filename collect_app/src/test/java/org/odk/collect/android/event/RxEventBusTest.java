package org.odk.collect.android.event;


import org.junit.Test;
import org.odk.collect.android.events.RxEvent;
import org.odk.collect.android.events.RxEventBus;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RxEventBusTest {

    /**
     * Verifies that when subscription takes place the Subject knows this has
     * happened.
     */
    @Test
    public void testEventBusSubscription() {

        RxEventBus bus = new RxEventBus();

        assertFalse(bus.getBusSubject().hasObservers());

        TestObserver<RxEvent> testObserver = bus.register(RxEvent.class).test();

        testObserver.assertSubscribed();

        assertTrue(bus.getBusSubject().hasObservers());
    }

    /**
     * Ensures that the register method of the RxEvent Bus is filtering events appropriately.
     * The assertValue method of TestObserver is used instead of AssertResult because
     * AssertResult is used to check for completeness and RxRelay's main objective is
     * to get rid of onComplete and onError that could damage a stream especially in the case
     * of a bus.
     */
    @Test
    public void testEventType() {
        RxEventBus bus = new RxEventBus();

        CoolerDummyEvent event = new CoolerDummyEvent();

        //RxEvent that won't get propagated to the observer.
        DummyEvent dummyEvent = new DummyEvent();

        TestObserver<CoolerDummyEvent> testObserver = bus.register(CoolerDummyEvent.class).test();

        bus.post(event);
        bus.post(dummyEvent);

        testObserver.assertValue(event);
    }

    class DummyEvent extends RxEvent {
    }

    class CoolerDummyEvent extends RxEvent {
    }
}
