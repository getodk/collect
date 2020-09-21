package org.odk.collect.android.configure;

import android.content.SharedPreferences;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.MetaKeys;
import org.odk.collect.android.preferences.utilities.ChangingServerUrlUtils;

import java.util.List;

public class SharedPreferencesServerRepository implements ServerRepository {

    @Override
    public void save(String url) {
        ChangingServerUrlUtils.addUrlToList(url);
    }

    @Override
    public List<String> getServers() {
        return ChangingServerUrlUtils.getUrlList();
    }

    @Override
    public void clear() {
        getSharedPreferences().edit().remove(MetaKeys.SERVER_LIST).apply();
    }

    private SharedPreferences getSharedPreferences() {
        return Collect.getInstance().getComponent().preferencesProvider().getMetaSharedPreferences();
    }
}
