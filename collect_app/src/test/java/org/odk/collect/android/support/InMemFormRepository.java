package org.odk.collect.android.support;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.utilities.MultiFormDownloader;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class InMemFormRepository implements FormRepository {

    private final List<Form> forms = new ArrayList<>();

    @Override
    public void save(Form form) {
        forms.add(form);
    }

    @Override
    public boolean contains(String jrFormId) {
        return forms.stream().anyMatch(f -> f.getJrFormId().equals(jrFormId));
    }

    @Override
    public List<Form> getAll() {
        return new ArrayList<>(forms); // Avoid anything  mutating the list externally
    }

    @Nullable
    @Override
    public Form getByMd5Hash(String hash) {
        return forms.stream().filter(f -> f.getMD5Hash().equals(hash)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Form getByLastDetectedUpdate(String formHash, String manifestHash) {
        String lastDetectedVersion = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

        return forms.stream().filter(f -> {
            String formLastDetectedVersion = f.getLastDetectedFormVersionHash();
            if (formLastDetectedVersion != null) {
                return formLastDetectedVersion.equals(lastDetectedVersion);
            } else {
                return false;
            }
        }).findFirst().orElse(null);
    }

    @Override
    public void delete(Long id) {
        forms.removeIf(form -> form.getId().equals(id));
    }

    @Override
    public void setLastDetectedUpdated(String jrFormId, String formHash, String manifestHash) {
        Form form = forms.stream().filter(f -> f.getJrFormId().equals(jrFormId)).findFirst().orElse(null);

        if (form != null) {
            forms.remove(form);
            forms.add(new Form.Builder(form)
                    .lastDetectedFormVersionHash(MultiFormDownloader.getMd5Hash(formHash) + manifestHash)
                    .build());
        }

    }
}
