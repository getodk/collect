package org.odk.collect.android.forms;

import java.util.List;

public interface FormRepository {
    void save(Form form);

    boolean contains(String jrFormID);

    List<Form> getAll();

    void delete(Long id);
}
