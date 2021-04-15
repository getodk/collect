package org.odk.collect.android.configure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.preferences.keys.MetaKeys;
import org.odk.collect.shared.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SharedPreferencesServerRepository implements ServerRepository {

    private final String defaultServer;
    private final Settings settings;

    public SharedPreferencesServerRepository(String defaultServer, Settings settings) {
        this.defaultServer = defaultServer;
        this.settings = settings;
    }

    @Override
    public void save(String url) {
        List<String> urlList = getServers();

        if (urlList.contains(url)) {
            urlList.remove(url);
        } else if (urlList.size() == 5) {
            urlList.remove(4);
        }

        urlList.add(0, url);
        settings.save(MetaKeys.SERVER_LIST, new Gson().toJson(urlList));
    }

    @Override
    public List<String> getServers() {
        String urlListString = settings.getString(MetaKeys.SERVER_LIST);
        return urlListString == null || urlListString.isEmpty()
                ? new ArrayList<>(Collections.singletonList(defaultServer))
                : new Gson().fromJson(urlListString, new TypeToken<List<String>>() {}.getType());
    }

    @Override
    public void clear() {
        settings.remove(MetaKeys.SERVER_LIST);
    }
}
