package odk.hedera.collect.utilities;

import android.net.Uri;

public interface FileProvider {
    Uri getURIForFile(String filePath);
}
