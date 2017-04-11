package org.odk.collect.android.utilities;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by simran on 4/10/2017.
 */

public class RegexMatcher extends TypeSafeMatcher<String> {

    private final String regex;

    public RegexMatcher(final String regex) {
        this.regex = regex;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("matches regex=`" + regex + "`");
    }

    @Override
    public boolean matchesSafely(final String string) {
        return string.matches(regex);
    }


    public static RegexMatcher matchesRegex(final String regex) {
        return new RegexMatcher(regex);
    }


}
