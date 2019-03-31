package org.odk.collect.android.utilities.rx;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;


public class TestSchedulerProvider implements SchedulerProvider {

    public TestSchedulerProvider() {
    }

    @Override
    public Scheduler computation() {
        return Schedulers.trampoline();
    }

    @Override
    public Scheduler io() {
        return Schedulers.trampoline();
    }

    @Override
    public Scheduler ui() {
        return Schedulers.trampoline();
    }
}
