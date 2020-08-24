package org.odk.collect.android.database;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.forms.MediaFileRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class DatabaseMediaFileRepository implements MediaFileRepository {

    @Override
    public List<File> getAll(String jrFormID, String formVersion) {
        String formMediaPath = new FormsDao().getFormMediaPath(jrFormID, formVersion);
        return formMediaPath == null
                ? new ArrayList<>()
                : asList(new File(formMediaPath).listFiles());
    }
}
