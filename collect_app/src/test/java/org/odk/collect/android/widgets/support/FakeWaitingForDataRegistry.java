package org.odk.collect.android.widgets.support;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.util.ArrayList;
import java.util.List;

public class FakeWaitingForDataRegistry implements WaitingForDataRegistry {

    public List<FormIndex> waiting = new ArrayList<>();

    @Override
    public void waitForData(FormIndex index) {
        waiting.add(index);
    }

    @Override
    public boolean isWaitingForData(FormIndex index) {
        return waiting.contains(index);
    }

    @Override
    public void cancelWaitingForData() {
        waiting.clear();
    }
}
