package org.odk.collect.android.utilities;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;

public interface ContentUriFetcher {
    Uri getUri(@NonNull Context context, @NonNull String authority, @NonNull File file);
}
