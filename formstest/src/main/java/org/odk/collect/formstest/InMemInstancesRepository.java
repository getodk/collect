package org.odk.collect.formstest;

import org.apache.commons.io.FileUtils;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class InMemInstancesRepository implements InstancesRepository {

    private final List<Instance> instances;
    private final Supplier<Long> clock;

    private long idCounter = 1L;

    public InMemInstancesRepository() {
        this(System::currentTimeMillis, new ArrayList<>());
    }

    public InMemInstancesRepository(List<Instance> instances) {
        this(System::currentTimeMillis, instances);
    }

    public InMemInstancesRepository(Supplier<Long> clock) {
        this(clock, new ArrayList<>());
    }

    public InMemInstancesRepository(Supplier<Long> clock, List<Instance> instances) {
        this.clock = clock;
        this.instances = new ArrayList<>(instances);
    }

    @Override
    public Instance get(Long databaseId) {
        for (Instance instance : instances) {
            if (instance.getDbId().equals(databaseId)) {
                return instance;
            }
        }

        return null;
    }

    @Override
    public Instance getOneByPath(String instancePath) {
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.getInstanceFilePath().equals(instancePath)) {
                result.add(instance);
            }
        }

        if (result.size() == 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Instance> getAll() {
        return new ArrayList<>(instances);
    }

    @Override
    public List<Instance> getAllNotDeleted() {
        return instances.stream()
                .filter(instance -> instance.getDeletedDate() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Instance> getAllByStatus(String... status) {
        List<String> statuses = Arrays.asList(status);
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (statuses.contains(instance.getStatus())) {
                result.add(instance);
            }
        }

        return result;
    }

    @Override
    public int getCountByStatus(String... status) {
        return getAllByStatus(status).size();
    }

    @Override
    public List<Instance> getAllByFormId(String formId) {
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.getFormId().equals(formId)) {
                result.add(instance);
            }
        }

        return result;
    }

    @Override
    public List<Instance> getAllNotDeletedByFormIdAndVersion(String formId, String version) {
        return instances.stream().filter(instance -> {
            return Objects.equals(instance.getFormId(), formId)
                    && Objects.equals(instance.getFormVersion(), version)
                    && instance.getDeletedDate() == null;
        }).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        Instance instance = get(id);
        deleteInstanceFiles(instance);

        instances.remove(instance);
    }

    @Override
    public void deleteAll() {
        for (Instance instance : instances) {
            deleteInstanceFiles(instance);
        }

        instances.clear();
    }

    @Override
    public Instance save(Instance instance) {
        if (instance.getStatus() == null) {
            instance = new Instance.Builder(instance)
                    .status(Instance.STATUS_INCOMPLETE)
                    .build();
        }

        Long id = instance.getDbId();
        if (id == null) {
            if (instance.getLastStatusChangeDate() == null) {
                instance = new Instance.Builder(instance)
                        .lastStatusChangeDate(clock.get())
                        .build();
            }

            Instance newInstance = new Instance.Builder(instance)
                    .dbId(idCounter++)
                    .build();
            instances.add(newInstance);
            return newInstance;
        } else {
            if (instance.getDeletedDate() == null) {
                instance = new Instance.Builder(instance)
                        .lastStatusChangeDate(clock.get())
                        .build();
            }

            instances.removeIf(i -> i.getDbId().equals(id));
            instances.add(instance);
            return instance;
        }
    }

    @Override
    public void deleteWithLogging(Long id) {
        Instance instance = new Instance.Builder(get(id))
                .geometry(null)
                .geometryType(null)
                .deletedDate(clock.get())
                .build();

        instances.removeIf(i -> i.getDbId().equals(id));
        instances.add(instance);
        deleteInstanceFiles(instance);
    }

    public void removeInstanceById(Long databaseId) {
        for (int i = 0; i < instances.size(); i++) {
            if (instances.get(i).getDbId().equals(databaseId)) {
                instances.remove(i);
                return;
            }
        }
    }

    private void deleteInstanceFiles(Instance instance) {
        try {
            FileUtils.deleteDirectory(new File(instance.getInstanceFilePath()).getParentFile());
        } catch (IOException e) {
            // Ignored
        }
    }
}
