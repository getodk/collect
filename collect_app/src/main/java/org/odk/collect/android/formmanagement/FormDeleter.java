package org.odk.collect.android.formmanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;

import java.util.List;

public class FormDeleter {

    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;

    public FormDeleter(FormsRepository formsRepository, InstancesRepository instancesRepository) {
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
    }

    public void delete(Long id) {
        Form form = formsRepository.get(id);
        List<Instance> instancesForVersion = instancesRepository.getAllByJrFormIdAndJrVersion(form.getJrFormId(), form.getJrVersion());

        if (instancesForVersion.isEmpty()) {
            formsRepository.delete(id);
        } else {
            formsRepository.softDelete(form.getId());
        }
    }
}
