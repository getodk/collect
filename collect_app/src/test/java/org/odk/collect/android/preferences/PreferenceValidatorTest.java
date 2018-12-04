package org.odk.collect.android.preferences;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreferenceValidatorTest {

    private final Map<String, Object> defaults = new HashMap<>();
    private final Map<String, Object> newValues = new HashMap<>();
    private final PreferenceValidator validator = new PreferenceValidator(defaults);

    @Test
    public void allowsNullNewValues() {
        defaults.put("null", false);
        newValues.put("null", null);
        assertTrue(validator.isValid(newValues));
    }

    @Test
    public void allowsNewAndMissingKeys() {
        defaults.put("one", false);
        newValues.put("two", false);
        assertTrue(validator.isValid(newValues));
    }

    @Test
    public void validatesBooleanValues() {
        defaults.put("boolean", false);
        newValues.put("boolean", "blah");
        assertFalse(validator.isValid(newValues));
    }

    @Test
    public void validatesStringValues() {
        defaults.put("string", "blah");
        newValues.put("string", false);
        assertFalse(validator.isValid(newValues));
    }

    @Test
    public void validatesIntValues() {
        defaults.put("int", 1);
        newValues.put("int", "blah");
        assertFalse(validator.isValid(newValues));
    }

    @Test
    public void validatesLongValues() {
        defaults.put("long", 1L);
        newValues.put("long", "blah");
        assertFalse(validator.isValid(newValues));
    }

    @Test
    public void validatesFloatValues() {
        defaults.put("float", 1f);
        newValues.put("float", "blah");
        assertFalse(validator.isValid(newValues));
    }

    @Test
    public void isValidPreference_validatesStringSetValues() {
        defaults.put("string_set", new HashSet<String>());
        newValues.put("string_set", "blah");
        assertFalse(validator.isValid(newValues));
    }
}