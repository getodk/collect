package org.odk.collect.android.formmanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.itemsets.FastExternalItemsetsRepository;

import java.io.File;
import java.util.List;

public class FormDeleter {

    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;
    private final FastExternalItemsetsRepository fastExternalItemsetsRepository;

    public FormDeleter(FormsRepository formsRepository, InstancesRepository instancesRepository, FastExternalItemsetsRepository fastExternalItemsetsRepository) {
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
        this.fastExternalItemsetsRepository = fastExternalItemsetsRepository;
    }

    public void delete(Long id) {
        Form form = formsRepository.get(id);

        List<Instance> instancesForVersion = instancesRepository.getAllNotDeletedByFormIdAndVersion(form.getFormId(), form.getVersion());
        // If there's more than one form with the same formid/version, trust the user that they want to truly delete this one
        // because otherwise it may not ever be removed (instance deletion only deletes one corresponding form).
        List<Form> formsWithSameFormIdVersion = formsRepository.getAllByFormIdAndVersion(form.getFormId(), form.getVersion());
        if (instancesForVersion.isEmpty() || formsWithSameFormIdVersion.size() > 1) {
            formsRepository.delete(id);
        } else {
            formsRepository.softDelete(form.getDbId());
        }

        fastExternalItemsetsRepository.deleteAllByCsvPath(form.getFormMediaPath() + File.separator + "itemsets.csv");
    }
}
