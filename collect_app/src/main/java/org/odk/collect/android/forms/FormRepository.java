package org.odk.collect.android.forms;

import java.util.List;

import javax.annotation.Nullable;

public interface FormRepository {

    void save(Form form);

    boolean contains(String jrFormID);

    List<Form> getAll();

    @Nullable
    Form getByMd5Hash(String hash);

    void delete(Long id);
}
