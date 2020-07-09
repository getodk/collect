package org.odk.collect.android.forms;

import java.io.File;
import java.util.List;

public interface MediaFileRepository {

    List<File> getAll(String jrFormID, String formVersion);
}
