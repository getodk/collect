package org.odk.collect.android.instancemanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;

public class InstanceDeleter {

    private final InstancesRepository instancesRepository;
    private final FormsRepository formsRepository;

    public InstanceDeleter(InstancesRepository instancesRepository, FormsRepository formsRepository) {
        this.instancesRepository = instancesRepository;
        this.formsRepository = formsRepository;
    }

    public void delete(Long id) {
        Instance instance = instancesRepository.get(id);
        if (instancesRepository.getAllByJrFormIdAndJrVersion(instance.getJrFormId(), instance.getJrVersion()).size() == 1) {
            Form form = formsRepository.get(instance.getJrFormId(), instance.getJrVersion());

            if (form != null && form.isDeleted()) {
                formsRepository.delete(form.getId());
            }
        }

        instancesRepository.delete(id);
    }
}
