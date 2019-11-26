package org.odk.collect.android.http;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public interface CaseInsensitiveHeaders {
    public @Nullable Set<String> getHeaders();

    public boolean containsHeader(String header);

    public @Nullable String getAnyValue(String header);
    public @Nullable List<String> getValues(String header);
}
