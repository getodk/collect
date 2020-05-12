package org.odk.collect.android.preferences;

import androidx.test.core.app.ApplicationProvider;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.preferences.ProtocolPreferenceMapper.Protocol;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
public class ProtocolPreferenceMapperTest {

    private ProtocolPreferenceMapper mapper;

    @Before
    public void setup() {
        mapper = new ProtocolPreferenceMapper(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void whenPreferenceValueIsNull_returnsODK() {
        assertThat(mapper.getProtocol(null), is(Protocol.ODK));
    }

    @Test
    public void whenPreferenceValueIsODK_returnsODK() {
        assertThat(mapper.getProtocol(getString(R.string.protocol_odk_default)), is(Protocol.ODK));
    }

    @Test
    public void whenPreferenceValueIsGoogle_returnsGoogle() {
        assertThat(mapper.getProtocol(getString(R.string.protocol_google_sheets)), is(Protocol.GOOGLE));
    }

    @Test
    public void whenPreferenceValueIsUnrecognized_returnsODK() {
        assertThat(mapper.getProtocol("bogus"), is(Protocol.ODK));
    }


    @NotNull
    private String getString(int resourceID) {
        return ApplicationProvider.getApplicationContext().getString(resourceID);
    }
}