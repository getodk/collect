package org.odk.collect.android.configure;

import org.json.JSONObject;
import org.junit.Test;
import org.odk.collect.android.configure.qr.AppConfigurationKeys;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class StructureAndTypeSettingsValidatorTest {

    private final Map<String, Object> generalDefaults = new HashMap<String, Object>() {{
        put("key1", true);
    }};

    private final Map<String, Object> adminDefaults = new HashMap<String, Object>() {{
        put("key1", "default");
    }};

    private final StructureAndTypeSettingsValidator validator = new StructureAndTypeSettingsValidator(generalDefaults, adminDefaults);

    @Test
    public void forEmptySettings_returnsTrue() throws Exception {
        assertThat(validator.isValid(emptySettings()), is(true));
    }

    @Test
    public void forNonJSON_returnsFalse() {
        assertThat(validator.isValid("blah"), is(false));
    }

    @Test
    public void withoutGeneralObject_returnsFalse() throws Exception {
        JSONObject json = emptySettingsObject();
        json.remove(AppConfigurationKeys.GENERAL); // Remove mutates and returns removed item :(

        assertThat(validator.isValid(json.toString()), is(false));
    }

    @Test
    public void withoutAdminObject_returnsFalse() throws Exception {
        JSONObject json = emptySettingsObject();
        json.remove(AppConfigurationKeys.ADMIN); // Remove mutates and returns removed item :(

        assertThat(validator.isValid(json.toString()), is(false));
    }

    @Test
    public void ifGeneralValueDoesNotMatchDefaultType_returnsFalse() throws Exception {
        JSONObject json = emptySettingsObject()
                .put(AppConfigurationKeys.GENERAL, new JSONObject()
                        .put("key1", "string"));

        assertThat(validator.isValid(json.toString()), is(false));
    }

    @Test
    public void ifAdminValueDoesNotMatchDefaultType_returnsFalse() throws Exception {
        JSONObject json = emptySettingsObject()
                .put(AppConfigurationKeys.ADMIN, new JSONObject()
                        .put("key1", false));

        assertThat(validator.isValid(json.toString()), is(false));
    }

    private String emptySettings() throws Exception {
        return emptySettingsObject()
                .toString();
    }

    private JSONObject emptySettingsObject() throws Exception {
        return new JSONObject()
                .put(AppConfigurationKeys.GENERAL, new JSONObject())
                .put(AppConfigurationKeys.ADMIN, new JSONObject());
    }
}