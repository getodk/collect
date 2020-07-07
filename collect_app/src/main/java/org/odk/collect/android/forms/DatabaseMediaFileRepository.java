package org.odk.collect.android.forms;

import org.odk.collect.android.dao.FormsDao;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;

public class DatabaseMediaFileRepository implements MediaFileRepository {

    @Override
    public List<File> getAll(String jrFormID, String formVersion) {
        String formMediaPath = new FormsDao().getFormMediaPath(jrFormID, formVersion);
        File[] files = new File(formMediaPath).listFiles();
        return asList(files);
    }
}
