package org.odk.collect.android;

import org.junit.Before;
import org.odk.collect.android.location.model.MapFunction;
import org.odk.collect.android.location.model.MapType;

/**
 * @author James Knight
 */

public abstract class GeoViewModelTest {

    final MapType type;
    final MapFunction function;

    protected GeoViewModelTest(MapType type, MapFunction function) {
        this.type = type;
        this.function = function;
    }

    @Before
    public void setInitialState() {
    }
}
