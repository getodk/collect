package org.odk.collect.android.forms;

import java.util.List;

import javax.annotation.Nullable;

public interface FormRepository {

    void save(Form form);

    boolean contains(String jrFormId);

    List<Form> getAll();

    @Nullable
    Form getByMd5Hash(String hash);

    @Nullable
    Form getByLastDetectedUpdate(String formHash, String manifestHash);

    void delete(Long id);

    void setLastDetectedUpdated(String jrFormId, String formHash, String manifestHash);
}
