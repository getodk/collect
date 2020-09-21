package org.odk.collect.android.configure;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.MetaKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SharedPreferencesServerRepository implements ServerRepository {

    @Override
    public void save(String url) {
        List<String> urlList = getServers();

        if (urlList.contains(url)) {
            urlList.remove(url);
        } else if (urlList.size() == 5) {
            urlList.remove(4);
        }

        urlList.add(0, url);
        getSharedPreferences().edit().putString(MetaKeys.SERVER_LIST, new Gson().toJson(urlList)).apply();
    }

    @Override
    public List<String> getServers() {
        String urlListString = getSharedPreferences().getString(MetaKeys.SERVER_LIST, null);

        return urlListString == null || urlListString.isEmpty()
                ? new ArrayList<>(Collections.singletonList(Collect.getInstance().getString(R.string.default_server_url)))
                : new Gson().fromJson(urlListString, new TypeToken<List<String>>() {}.getType());
    }

    @Override
    public void clear() {
        getSharedPreferences().edit().remove(MetaKeys.SERVER_LIST).apply();
    }

    private SharedPreferences getSharedPreferences() {
        return Collect.getInstance().getComponent().preferencesProvider().getMetaSharedPreferences();
    }
}
