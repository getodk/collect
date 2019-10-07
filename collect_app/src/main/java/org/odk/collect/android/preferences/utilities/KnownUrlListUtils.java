package org.odk.collect.android.preferences.utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KnownUrlListUtils {
    static final String KNOWN_URL_LIST = "knownUrlList";

    private KnownUrlListUtils() {
    }

    public static void addUrlToList(String url) {
        List<String> urlList = getUrlList();

        if (urlList.contains(url)) {
            urlList.remove(url);
        } else if (urlList.size() == 5) {
            if (urlList.get(4).equals(Collect.getInstance().getString(R.string.default_server_url))) {
                urlList.remove(3);
            } else {
                urlList.remove(4);
            }
        }

        urlList.add(0, url);
        GeneralSharedPreferences.getInstance().save(KNOWN_URL_LIST, new Gson().toJson(urlList));
    }

    public static List<String> getUrlList() {
        String urlListString = (String) GeneralSharedPreferences.getInstance().get(KNOWN_URL_LIST);

        return urlListString == null || urlListString.isEmpty()
                ? new ArrayList<>(Collections.singletonList(Collect.getInstance().getString(R.string.default_server_url)))
                : new Gson().fromJson(urlListString, new TypeToken<List<String>>() {}.getType());
    }
}
