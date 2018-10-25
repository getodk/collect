package org.odk.collect.android.preferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.odk.collect.android.preferences.PreferenceKeys.GENERAL_KEYS;

@RunWith(RobolectricTestRunner.class)
public class GeneralSharedPreferencesTest {

    private GeneralSharedPreferences instance;

    @Before
    public void setup() {
        instance = GeneralSharedPreferences.getInstance();
    }

    @After
    public void teardown() {
        // Account for GENERAL_KEYS being static state
        GENERAL_KEYS.remove("null");
        GENERAL_KEYS.remove("boolean");
        GENERAL_KEYS.remove("string");
        GENERAL_KEYS.remove("int");
        GENERAL_KEYS.remove("long");
        GENERAL_KEYS.remove("float");
        GENERAL_KEYS.remove("string_set");
    }

    @Test
    public void save_allowsNullValues() {
        GENERAL_KEYS.put("null", false);
        instance.save("null", null);
    }

    @Test(expected = GeneralSharedPreferences.ValidationException.class)
    public void save_validatesBooleanValues() {
        GENERAL_KEYS.put("boolean", false);
        instance.save("boolean", "blah");
        assertEquals(instance.get("boolean"), false);
    }

    @Test(expected = GeneralSharedPreferences.ValidationException.class)
    public void save_validatesStringValues() {
        GENERAL_KEYS.put("string", "blah");
        instance.save("string", false);
        assertEquals(instance.get("string"), "blah");
    }
    @Test(expected = GeneralSharedPreferences.ValidationException.class)
    public void save_validatesIntValues() {
        GENERAL_KEYS.put("int", 1);
        instance.save("int", false);
        assertEquals(instance.get("int"), 1);
    }

    @Test(expected = GeneralSharedPreferences.ValidationException.class)
    public void save_validatesLongValues() {
        GENERAL_KEYS.put("long", 1L);
        instance.save("long", false);
        assertEquals(instance.get("long"), 1L);
    }

    @Test(expected = GeneralSharedPreferences.ValidationException.class)
    public void save_validatesFloatValues() {
        GENERAL_KEYS.put("float", 1f);
        instance.save("float", false);
        assertEquals(instance.get("float"), 1f);
    }

    @Test(expected = GeneralSharedPreferences.ValidationException.class)
    public void save_validatesStringSetValues() {
        GENERAL_KEYS.put("string_set", new HashSet<String>());
        instance.save("string_set", false);
        assertEquals(instance.get("string_set"), new HashSet<String>());
    }
}