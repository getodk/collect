package org.odk.collect.android.forms;

import android.net.Uri;

import java.util.List;

import javax.annotation.Nullable;

public interface FormsRepository {
    @Nullable
    Form get(Long id);

    @Nullable
    Form getOneByFormIdAndVersion(String formId, @Nullable String version);

    @Nullable
    Form getOneByPath(String path);

    @Nullable
    Form getOneByMd5Hash(String hash);

    List<Form> getAll();

    List<Form> getAllByFormIdAndVersion(String formId, @Nullable String version);

    List<Form> getAllNotDeletedByFormId(String formId);

    List<Form> getAllNotDeletedByFormIdAndVersion(String formId, @Nullable String version);

    Uri save(Form form);

    void delete(Long id);

    void softDelete(Long id);

    void deleteByMd5Hash(String md5Hash);

    void restore(Long id);
}
