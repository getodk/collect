package org.odk.collect.forms.instances;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Gives access to {@link Instance} objects representing filled form instances on the device.
 *
 * The goal of this layer is to separate domain objects from data fetching concerns to facilitate
 * testing and reduce duplicated code to access those domain objects. To start, it has specific
 * query methods. Over time, we should expand it to mediate addition and removal. If there ends up
 * being may specific query methods, we may want to introduce a way to query more generically
 * without introducing new specialized methods (e.g. get(Specification s) instead of getBy(XYZ).
 */
public interface InstancesRepository {

    @Nullable
    Instance get(Long id);

    /**
     * Null if not exactly one instance matches.
     */
    @Nullable
    Instance getOneByPath(String instancePath);

    List<Instance> getAll();

    List<Instance> getAllNotDeleted();

    List<Instance> getAllByStatus(String... status);

    int getCountByStatus(String... status);

    List<Instance> getAllByFormId(String formId);

    List<Instance> getAllNotDeletedByFormIdAndVersion(String formId, String version);

    void delete(Long id);

    void deleteAll();

    Instance save(Instance instance);

    /**
     * Deletes any sensitive data (the submission file and geometry data) but leaves the Instance
     * record in place.
     */
    void deleteWithLogging(Long id);
}
