package org.odk.collect.android.configure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.preferences.MetaKeys;
import org.odk.collect.android.preferences.PreferencesDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SharedPreferencesServerRepository implements ServerRepository {

    private final String defaultServer;
    private final PreferencesDataSource preferencesDataSource;

    public SharedPreferencesServerRepository(String defaultServer, PreferencesDataSource preferencesDataSource) {
        this.defaultServer = defaultServer;
        this.preferencesDataSource = preferencesDataSource;
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
        preferencesDataSource.save(MetaKeys.SERVER_LIST, new Gson().toJson(urlList));
    }

    @Override
    public List<String> getServers() {
        String urlListString = preferencesDataSource.getString(MetaKeys.SERVER_LIST);
        return urlListString == null || urlListString.isEmpty()
                ? new ArrayList<>(Collections.singletonList(defaultServer))
                : new Gson().fromJson(urlListString, new TypeToken<List<String>>() {}.getType());
    }

    @Override
    public void clear() {
        preferencesDataSource.remove(MetaKeys.SERVER_LIST);
    }
}
