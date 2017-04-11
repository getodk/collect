package org.odk.collect.android.utilities;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;



public class RegexMatcher extends TypeSafeMatcher<String> {

    private  String regex;

    public RegexMatcher( String regex) {
        this.regex = regex;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("matches regex=" + regex + "");
    }

    @Override
    public boolean matchesSafely(final String string) {
        return string.matches(regex);
    }


    public static RegexMatcher matchesRegex(String regex) {
        return new RegexMatcher(regex);
    }
}
