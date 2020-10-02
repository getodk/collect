package org.odk.collect.android.forms;

import android.net.Uri;

import java.util.List;

import javax.annotation.Nullable;

public interface FormsRepository {

    Uri save(Form form);

    List<Form> getAll();

    @Nullable
    Form get(Long id);

    @Nullable
    Form get(String jrFormId, @Nullable String jrVersion);

    List<Form> getByJrFormIdNotDeleted(String jrFormId);

    @Nullable
    Form getByMd5Hash(String hash);

    @Nullable
    Form getByPath(String path);

    void delete(Long id);

    void softDelete(Long id);

    void deleteFormsByMd5Hash(String md5Hash);

    void restore(Long id);
}
