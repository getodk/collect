package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PrefMigrator.Copier;
import org.odk.collect.android.preferences.PrefMigrator.Migration;
import org.odk.collect.android.preferences.PrefMigrator.Translator;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class PrefMigratorTest {

    private SharedPreferences prefs;
    private static final Migration[] COPIERS = {
        new Copier("couleur", "colour"),
        new Copier("legume", "vegetable")
    };
    private static final Migration[] TRANSLATORS = {
        new Translator("couleur", "colour")
            .withValue("rouge", "red")
            .withValue("vert", "green"),
        new Translator("legume", "vegetable")
            .withValue("tomate", "tomato")
            .withValue("aubergine", "eggplant")
    };
    private static final Migration[] KEY_SPLITTER = {
        new Translator("pear", "fruit")
            .withValue("anjou", "pear_anjou")
            .withValue("bartlett", "pear_bartlett"),
        new Translator("melon", "fruit")
            .withValue("cantaloupe", "melon_cantaloupe")
            .withValue("watermelon", "melon_watermelon")
    };
    private static final Migration[] KEY_MERGER = {
        new Translator("fruit", "pear")
            .withValue("pear_anjou", "anjou")
            .withValue("pear_bartlett", "bartlett"),
        new Translator("fruit", "melon")
            .withValue("melon_cantaloupe", "cantaloupe")
            .withValue("melon_watermelon", "watermelon")
    };

    @Before public void setUp() throws Exception {
        prefs = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
    }

    private void initPrefs(Object... pairs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            if (pairs[i + 1] instanceof String) {
                editor.putString((String) pairs[i], (String) pairs[i + 1]);
            } else if (pairs[i + 1] instanceof Boolean) {
                editor.putBoolean((String) pairs[i], (Boolean) pairs[i + 1]);
            }
        }
        editor.commit();
    }

    private void assertPrefs(Object... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            assertEquals(pairs[i + 1], prefs.getAll().get(pairs[i]));
        }
        assertEquals(pairs.length/2, prefs.getAll().size());
    }

    @Test public void shouldCopyValues() {
        initPrefs(
            "colour", "red",
            "vegetable", "eggplant"
        );
        PrefMigrator.migrate(prefs, COPIERS);
        assertPrefs(
            "couleur", "red",
            "legume", "eggplant"
        );
    }

    @Test public void shouldTranslateValues() {
        initPrefs(
            "colour", "red",
            "vegetable", "eggplant"
        );
        PrefMigrator.migrate(prefs, TRANSLATORS);
        assertPrefs(
            "couleur", "rouge",
            "legume", "aubergine"
        );
    }

    @Test public void shouldNotTranslateUnknownValues() {
        initPrefs(
            "colour", "blue",
            "vegetable", "eggplant"
        );
        PrefMigrator.migrate(prefs, TRANSLATORS);
        assertPrefs(
            "colour", "blue",
            "legume", "aubergine"
        );
    }

    @Test public void shouldNotReplaceExistingNewKeys() {
        initPrefs(
            "colour", "red",
            "vegetable", "eggplant",
            "couleur", "bleu",
            "legume", "tomate",
            "network", true,
            "connection", "wifi"
        );
        PrefMigrator.migrate(prefs, COPIERS);
        assertPrefs(
            "colour", "red",
            "vegetable", "eggplant",
            "couleur", "bleu",
            "legume", "tomate",
            "network", true,
            "connection", "wifi"
        );
        PrefMigrator.migrate(prefs, TRANSLATORS);
        assertPrefs(
            "colour", "red",
            "vegetable", "eggplant",
            "couleur", "bleu",
            "legume", "tomate",
            "network", true,
            "connection", "wifi"
        );
    }

    @Test public void shouldSplitKeyAccordingToValue() {
        initPrefs("fruit", "pear_anjou");
        PrefMigrator.migrate(prefs, KEY_SPLITTER);
        assertPrefs("pear", "anjou");

        initPrefs("fruit", "melon_watermelon");
        PrefMigrator.migrate(prefs, KEY_SPLITTER);
        assertPrefs("melon", "watermelon");
    }

    @Test public void shouldMergeKeys() {
        initPrefs("pear", "anjou");
        PrefMigrator.migrate(prefs, KEY_MERGER);
        assertPrefs("fruit", "pear_anjou");

        initPrefs("melon", "watermelon");
        PrefMigrator.migrate(prefs, KEY_MERGER);
        assertPrefs("fruit", "melon_watermelon");
    }
}
