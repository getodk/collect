package org.odk.collect.android.tasks;



import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;



public class GoogleSheetsAbstractUploaderTest extends TypeSafeMatcher<String> {
    private  String regex;
    private GoogleSheetsAbstractUploaderTest(String regex) {
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

    public static GoogleSheetsAbstractUploaderTest matchesRegex(String regex) {
        return new GoogleSheetsAbstractUploaderTest(regex);
    }
}
