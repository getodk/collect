package org.odk.collect.android.preferences.utilities;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @deprecated This should probably all move into an implementation
 * of {@link org.odk.collect.android.configure.ServerRepository}
 */
@Deprecated
public class ChangingServerUrlUtils {
    
    public static final String KNOWN_URL_LIST = "knownUrlList";

    private ChangingServerUrlUtils() {
    }

    // The method adds a new url to the list if it doesn't already exist. The list is intended to
    // keep up to 5 elements so the oldest one is removed if needed.
    public static void addUrlToList(String url) {
        List<String> urlList = getUrlList();

        if (urlList.contains(url)) {
            urlList.remove(url);
        } else if (urlList.size() == 5) {
            urlList.remove(4);
        }

        urlList.add(0, url);
        getSharedPreferences().edit().putString(KNOWN_URL_LIST, new Gson().toJson(urlList)).apply();
    }

    public static List<String> getUrlList() {
        String urlListString = getSharedPreferences().getString(KNOWN_URL_LIST, null);

        return urlListString == null || urlListString.isEmpty()
                ? new ArrayList<>(Collections.singletonList(Collect.getInstance().getString(R.string.default_server_url)))
                : new Gson().fromJson(urlListString, new TypeToken<List<String>>() {}.getType());
    }

    private static SharedPreferences getSharedPreferences() {
        return Collect.getInstance().getComponent().preferencesProvider().getMetaSharedPreferences();
    }
}
