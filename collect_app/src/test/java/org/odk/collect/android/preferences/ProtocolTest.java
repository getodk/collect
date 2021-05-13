package org.odk.collect.android.preferences;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ProtocolTest {

    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void whenPreferenceValueIsNull_returnsODK() {
        assertThat(Protocol.parse(context, null), is(Protocol.ODK));
    }

    @Test
    public void whenPreferenceValueIsODK_returnsODK() {
        assertThat(Protocol.parse(context, getString(R.string.protocol_odk_default)), is(Protocol.ODK));
    }

    @Test
    public void whenPreferenceValueIsGoogle_returnsGoogle() {
        assertThat(Protocol.parse(context, getString(R.string.protocol_google_sheets)), is(Protocol.GOOGLE));
    }

    @Test
    public void whenPreferenceValueIsUnrecognized_returnsODK() {
        assertThat(Protocol.parse(context, "bogus"), is(Protocol.ODK));
    }


    @NotNull
    private String getString(int resourceID) {
        return ApplicationProvider.getApplicationContext().getString(resourceID);
    }
}