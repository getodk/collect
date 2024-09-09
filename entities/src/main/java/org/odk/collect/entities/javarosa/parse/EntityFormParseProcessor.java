package org.odk.collect.entities.javarosa.parse;

import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.XFormParser;
import org.odk.collect.entities.javarosa.spec.EntityFormParser;
import org.odk.collect.entities.javarosa.spec.UnrecognizedEntityVersionException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import kotlin.Pair;

public class EntityFormParseProcessor implements XFormParser.BindAttributeProcessor, XFormParser.FormDefProcessor, XFormParser.ModelAttributeProcessor {

    private static final String ENTITIES_NAMESPACE = "http://www.opendatakit.org/xforms/entities";
    private static final String[] SUPPORTED_VERSIONS = {"2022.1", "2023.1", "2024.1"};
    private static final String[] LOCAL_ENTITY_VERSIONS = {"2024.1"};

    private final List<Pair<XPathReference, String>> saveTos = new ArrayList<>();
    private String version;

    @Override
    public Set<Pair<String, String>> getModelAttributes() {
        HashSet<Pair<String, String>> attributes = new HashSet<>();
        attributes.add(new Pair<>(ENTITIES_NAMESPACE, "entities-version"));

        return attributes;
    }

    @Override
    public void processModelAttribute(String name, String value) throws XFormParser.ParseException {
        version = value;

        if (Stream.of(SUPPORTED_VERSIONS).noneMatch(value::startsWith)) {
            throw new UnrecognizedEntityVersionException();
        }
    }

    @Override
    public Set<Pair<String, String>> getBindAttributes() {
        HashSet<Pair<String, String>> attributes = new HashSet<>();
        attributes.add(new Pair<>(ENTITIES_NAMESPACE, "saveto"));

        return attributes;
    }

    @Override
    public void processBindAttribute(String name, String value, DataBinding binding) {
        saveTos.add(new Pair<>((XPathReference) binding.getReference(), value));
    }

    @Override
    public void processFormDef(FormDef formDef) throws XFormParser.ParseException {
        if (isEntityForm(formDef)) {
            if (version == null) {
                throw new XFormParser.MissingModelAttributeException(ENTITIES_NAMESPACE, "entities-version");
            } else if (Stream.of(LOCAL_ENTITY_VERSIONS).anyMatch(version::startsWith)) {
                EntityFormExtra entityFormExtra = new EntityFormExtra(saveTos);
                formDef.getExtras().put(entityFormExtra);
            }
        }
    }

    private static boolean isEntityForm(FormDef formDef) {
        return EntityFormParser.getEntityElement(formDef.getMainInstance()) != null;
    }
}
