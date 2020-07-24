package org.odk.collect.android.forms;

import android.net.Uri;

import java.util.List;

import javax.annotation.Nullable;

public interface FormRepository {

    Uri save(Form form);

    boolean contains(String jrFormId);

    List<Form> getAll();

    @Nullable
    Form getByMd5Hash(String hash);

    @Nullable
    Form getByLastDetectedUpdate(String formHash, String manifestHash);

    @Nullable
    Form getByPath(String path);

    void delete(Long id);

    void setLastDetectedUpdated(String jrFormId, String formHash, String manifestHash);

    void deleteFormsByMd5Hash(String md5Hash);
}
