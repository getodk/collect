package org.odk.collect.formstest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.shared.strings.Md5;
import org.odk.collect.shared.TempFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.deleteDirectory;

public class InMemFormsRepository implements FormsRepository {

    private final List<Form> forms = new ArrayList<>();
    private long idCounter = 1L;

    private final Supplier<Long> clock;

    public InMemFormsRepository() {
        this.clock = System::currentTimeMillis;
    }

    public InMemFormsRepository(Supplier<Long> clock) {
        this.clock = clock;
    }

    @Nullable
    @Override
    public Form get(Long id) {
        return forms.stream().filter(f -> f.getDbId().equals(id)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Form getLatestByFormIdAndVersion(String formId, @Nullable String version) {
        List<Form> candidates = getAllByFormIdAndVersion(formId, version);

        if (!candidates.isEmpty()) {
            return candidates.stream().max(Comparator.comparingLong(Form::getDate)).get();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Form getOneByMd5Hash(@NotNull String hash) {
        if (hash == null) {
            throw new IllegalArgumentException("null hash");
        }

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
        return forms.stream().filter(f -> f.getFormId().equals(jrFormId) && Objects.equals(f.getVersion(), jrVersion)).collect(toList());
    }

    @Override
    public List<Form> getAllByFormId(String formId) {
        return forms.stream().filter(f -> f.getFormId().equals(formId)).collect(toList());
    }

    @Override
    public List<Form> getAllNotDeletedByFormId(String jrFormId) {
        return forms.stream().filter(f -> f.getFormId().equals(jrFormId) && !f.isDeleted()).collect(toList());
    }

    public List<Form> getAllNotDeletedByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        return forms.stream().filter(f -> f.getFormId().equals(jrFormId) && Objects.equals(f.getVersion(), jrVersion) && !f.isDeleted()).collect(toList());
    }

    @Override
    public Form save(@NotNull Form form) {
        Form.Builder builder = new Form.Builder(form);

        if (form.getFormMediaPath() == null) {
            builder.formMediaPath(TempFiles.getPathInTempDir());
        }

        if (form.getDbId() != null) {
            String formFilePath = form.getFormFilePath();
            String hash = Md5.getMd5Hash(new File(formFilePath));
            builder.md5Hash(hash);

            forms.removeIf(f -> f.getDbId().equals(form.getDbId()));
            forms.add(builder.build());
            return form;
        } else {
            builder.dbId(idCounter++)
                    .date(clock.get());

            // Allows tests to override hash
            String hash;
            if (form.getMD5Hash() == null) {
                String formFilePath = form.getFormFilePath();
                hash = Md5.getMd5Hash(new File(formFilePath));
                builder.md5Hash(hash);
            } else {
                hash = form.getMD5Hash();
            }

            if (form.getJrCacheFilePath() == null) {
                builder.jrCacheFilePath(TempFiles.getPathInTempDir(hash, ".formdef"));
            }

            Form formToSave = builder.build();
            forms.add(formToSave);
            return formToSave;
        }
    }

    @Override
    public void delete(Long id) {
        Optional<Form> formToRemove = forms.stream().filter(f -> f.getDbId().equals(id)).findFirst();
        if (formToRemove.isPresent()) {
            Form form = formToRemove.get();
            deleteFilesForForm(form);
            forms.remove(form);
        }
    }

    @Override
    public void softDelete(Long id) {
        Form form = forms.stream().filter(f -> f.getDbId().equals(id)).findFirst().orElse(null);

        if (form != null) {
            forms.remove(form);
            forms.add(new Form.Builder(form)
                    .deleted(true)
                    .build());
        }
    }

    @Override
    public void deleteByMd5Hash(@NotNull String md5Hash) {
        forms.removeIf(f -> f.getMD5Hash().equals(md5Hash));
    }

    @Override
    public void deleteAll() {
        for (Form form : forms) {
            deleteFilesForForm(form);
        }

        forms.clear();
    }

    @Override
    public void restore(Long id) {
        Form form = forms.stream().filter(f -> f.getDbId().equals(id)).findFirst().orElse(null);

        if (form != null) {
            forms.remove(form);
            forms.add(new Form.Builder(form)
                    .deleted(false)
                    .build());
        }
    }

    private void deleteFilesForForm(Form form) {
        // Delete form file
        if (form.getFormFilePath() != null) {
            new File(form.getFormFilePath()).delete();
        }

        // Delete cache file
        if (form.getJrCacheFilePath() != null) {
            new File(form.getJrCacheFilePath()).delete();
        }

        // Delete media files
        if (form.getFormMediaPath() != null) {
            try {
                File mediaDir = new File(form.getFormMediaPath());

                if (mediaDir.isDirectory()) {
                    deleteDirectory(mediaDir);
                } else {
                    mediaDir.delete();
                }
            } catch (IOException ignored) {
                // Ignored
            }
        }
    }
}
