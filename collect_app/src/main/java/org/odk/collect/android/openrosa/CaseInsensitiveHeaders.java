package org.odk.collect.android.openrosa;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public interface CaseInsensitiveHeaders {
    @Nullable Set<String> getHeaders();

    boolean containsHeader(String header);

    @Nullable String getAnyValue(String header);

    @Nullable List<String> getValues(String header);
}
