package org.odk.collect.entities.javarosa;

import kotlin.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Entity {

    public final String dataset;
    public final List<Pair<String, String>> properties;

    @Nullable
    public final String id;

    @Nullable
    public final String label;

    public final Integer version;
    public final EntityAction action;

    public Entity(EntityAction action, String dataset, @Nullable String id, @Nullable String label, Integer version, List<Pair<String, String>> properties) {
        this.dataset = dataset;
        this.id = id;
        this.label = label;
        this.version = version;
        this.properties = properties;
        this.action = action;
    }
}
