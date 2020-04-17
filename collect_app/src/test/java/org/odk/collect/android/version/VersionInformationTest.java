package org.odk.collect.android.version;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class VersionInformationTest {

    @Test
    public void isRelease_whenDescriptionIsOneComponent_returnsTrue() {
        VersionInformation versionInformation = new VersionInformation(() -> "blah");
        assertThat(versionInformation.isRelease(), is(true));
    }

    @Test
    public void isRelease_whenDescriptionIsTwoComponents_returnsFalse() {
        VersionInformation versionInformation = new VersionInformation(() -> "something-blah");
        assertThat(versionInformation.isRelease(), is(false));
    }
}