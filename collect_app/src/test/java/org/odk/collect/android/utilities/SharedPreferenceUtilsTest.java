package org.odk.collect.android.utilities;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SHOW_SPLASH;

@RunWith(RobolectricTestRunner.class)
public class SharedPreferenceUtilsTest {
    private final GeneralSharedPreferences preferences = GeneralSharedPreferences.getInstance();

    @Test
    public void checkCorrectFormatPreferenceJsonString() throws JSONException {
        preferences.reloadPreferences();
        String preferencesString = SharedPreferencesUtils.getJSONFromPreferences(new ArrayList<>());
        JSONObject correctTypeSettingsJson = new JSONObject(preferencesString);
        JSONObject correctTypeSettingsGeneralJson = correctTypeSettingsJson.getJSONObject("general");
        correctTypeSettingsGeneralJson.put(KEY_SHOW_SPLASH, true);

        SharedPreferencesUtils.checkBasicTypesOnSettingsString(correctTypeSettingsJson.toString());

        JSONObject wrongTypeSettingsJson = new JSONObject(preferencesString);
        JSONObject wrongTypeSettingsGeneralJson = wrongTypeSettingsJson.getJSONObject("general");
        wrongTypeSettingsGeneralJson.put(KEY_SHOW_SPLASH, "true");
        boolean hasThrown = false;
        try{
            SharedPreferencesUtils.checkBasicTypesOnSettingsString(wrongTypeSettingsJson.toString());
        }catch (JSONException e){
            hasThrown = true;
        }

        assertTrue(hasThrown);

    }

}
