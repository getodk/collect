package org.odk.collect.android.preferences;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PreferenceKeysTest {

    @Test
    public void checkCorrectnessTypesOfDefaultsGeneralKeys(){
        HashMap<String, Class> expectedTypes = PreferenceKeys.getExpectedTypesForPreferencesValues();
        for (Map.Entry<String, Object> generalPref : PreferenceKeys.GENERAL_KEYS.entrySet()) {

            boolean foundKey = expectedTypes.containsKey(generalPref.getKey());
            //Check if all keys declared in GENERAL_KEYS has a declared type
            assertTrue(String.format("Not found default key %s from PreferenceKeys.GENERAL_KEYS in PreferenceKeys.getExpectedTypesForPreferencesValues()", generalPref.getKey()), foundKey);

            Object value = generalPref.getValue();
            Class desiredClass = expectedTypes.get(generalPref.getKey());

            //Check if the key has a correct declared type
            assertTrue(String.format("Incorrect type of default key %s from PreferenceKeys.GENERAL_KEYS", generalPref.getKey()), desiredClass.isInstance(value));
        }
    }
}
