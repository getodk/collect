package org.odk.collect.android.widgets.utilities;

import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class SearchQueryViewModel extends ViewModel {

    private final Map<String, String> queries = new HashMap<>();

    public String getQuery(String id) {
        return queries.getOrDefault(id, "");
    }

    public void setQuery(String id, String query) {
        queries.put(id, query);
    }
}
