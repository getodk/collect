package org.odk.collect.android.utilities.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.base.Preconditions;

/**
 * Concrete implementation of the {@link BaseResourceProvider} interface.
 */
public class ResourceProvider implements BaseResourceProvider {

    @NonNull
    private final Context context;

    public ResourceProvider(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context, "context cannot be null");
    }

    @NonNull
    @Override
    public String getString(@StringRes final int id) {
        return context.getString(id);
    }

    @NonNull
    @Override
    public String getString(@StringRes final int id, final Object... formatArgs) {
        return context.getString(id, formatArgs);
    }
}
