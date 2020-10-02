package org.odk.collect.android.database;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.utilities.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseMediaFileRepository implements MediaFileRepository {
    private final FormsDao formsDao;
    private final FileUtil fileUtil;

    public DatabaseMediaFileRepository(FormsDao formsDao, FileUtil fileUtil) {
        this.formsDao = formsDao;
        this.fileUtil = fileUtil;
    }

    @Override
    public List<File> getAll(String jrFormID, String formVersion) {
        String formMediaPath = formsDao.getFormMediaPath(jrFormID, formVersion);
        return formMediaPath == null
                ? new ArrayList<>()
                : fileUtil.listFiles(fileUtil.getFileAtPath(formMediaPath));
    }
}
