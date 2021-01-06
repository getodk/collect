package org.odk.collect.android.support;

import android.net.Uri;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import static java.util.stream.Collectors.toList;

public class InMemFormsRepository implements FormsRepository {

    private final List<Form> forms = new ArrayList<>();
    private long idCounter = 1L;

    @Nullable
    @Override
    public Form get(Long id) {
        return forms.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Form getOneByFormIdAndVersion(String formId, @Nullable String version) {
        return forms.stream().filter(f -> f.getJrFormId().equals(formId) && Objects.equals(f.getJrVersion(), version)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Form getOneByMd5Hash(String hash) {
        return forms.stream().filter(f -> f.getMD5Hash().equals(hash)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Form getOneByPath(String path) {
        return forms.stream().filter(f -> f.getFormFilePath().equals(path)).findFirst().orElse(null);
    }

    @Override
    public List<Form> getAll() {
        return new ArrayList<>(forms); // Avoid anything  mutating the list externally
    }

    @Override
    public List<Form> getAllByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        return forms.stream().filter(f -> f.getJrFormId().equals(jrFormId) && Objects.equals(f.getJrVersion(), jrVersion)).collect(toList());
    }

    @Override
    public List<Form> getAllNotDeletedByFormId(String jrFormId) {
        return forms.stream().filter(f -> f.getJrFormId().equals(jrFormId) && !f.isDeleted()).collect(toList());
    }

    public List<Form> getAllNotDeletedByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        return forms.stream().filter(f -> f.getJrFormId().equals(jrFormId) && Objects.equals(f.getJrVersion(), jrVersion) && !f.isDeleted()).collect(toList());
    }

    @Override
    public Uri save(Form form) {
        if (form.getId() == null) {
            form = new Form.Builder(form)
                    .id(idCounter++)
                    .build();
        }

        String formFilePath = form.getFormFilePath();

        if (formFilePath != null) {
            String hash = FileUtils.getMd5Hash(new File(formFilePath));
            forms.add(new Form.Builder(form)
                    .md5Hash(hash)
                    .build()
            );
        } else {
            forms.add(form);
        }

        return null;
    }

    @Override
    public void delete(Long id) {
        forms.removeIf(form -> form.getId().equals(id));
    }

    @Override
    public void softDelete(Long id) {
        Form form = forms.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);

        if (form != null) {
            forms.remove(form);
            forms.add(new Form.Builder(form)
                    .deleted(true)
                    .build());
        }
    }

    @Override
    public void deleteByMd5Hash(String md5Hash) {
        forms.removeIf(f -> f.getMD5Hash().equals(md5Hash));
    }

    @Override
    public void restore(Long id) {
        Form form = forms.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);

        if (form != null) {
            forms.remove(form);
            forms.add(new Form.Builder(form)
                    .deleted(false)
                    .build());
        }
    }
}
