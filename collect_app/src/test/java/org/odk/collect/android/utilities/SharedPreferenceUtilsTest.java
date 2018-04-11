package org.odk.collect.android.utilities;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SharedPreferenceUtilsTest {

    @Test
    public void checkTypesOnAJsonObjectTest() throws JSONException {
        //Create KEYs with desired type
        String BOOLEAN_KEY = "BOOL";
        String STRING_KEY = "STRING";

        //Init some variables
        JSONObject jsonObject = new JSONObject();
        HashMap<String, Class> expectedTypes = new HashMap<>();
        expectedTypes.put(BOOLEAN_KEY, Boolean.class);
        expectedTypes.put(STRING_KEY, String.class);

        //Expected boolean in value
        jsonObject.put(BOOLEAN_KEY, true);
        SharedPreferencesUtils.checkTypesOnJson(jsonObject, expectedTypes);

        //Expected String in value
        jsonObject.put(STRING_KEY, "value");
        SharedPreferencesUtils.checkTypesOnJson(jsonObject, expectedTypes);

        //Expected boolean in value and has a String
        jsonObject.put(BOOLEAN_KEY, "true");
        boolean hasThrown = false;
        try {
            SharedPreferencesUtils.checkTypesOnJson(jsonObject, expectedTypes);
        } catch (JSONException e) {
            hasThrown = true;
        }
        assertTrue(hasThrown);
    }
}
