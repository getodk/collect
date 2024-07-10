package org.odk.collect.entities.javarosa.finalization;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryFinalizationProcessor;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XPathReference;
import org.odk.collect.entities.javarosa.spec.EntityAction;
import org.odk.collect.entities.javarosa.spec.EntityFormParser;
import org.odk.collect.entities.javarosa.parse.EntityFormExtra;

import java.util.List;
import java.util.stream.Collectors;

import kotlin.Pair;

public class EntityFormFinalizationProcessor implements FormEntryFinalizationProcessor {

    @Override
    public void processForm(FormEntryModel formEntryModel) {
        FormDef formDef = formEntryModel.getForm();
        FormInstance mainInstance = formDef.getMainInstance();

        EntityFormExtra entityFormExtra = formDef.getExtras().get(EntityFormExtra.class);
        List<Pair<XPathReference, String>> saveTos = entityFormExtra.getSaveTos();

        TreeElement entityElement = EntityFormParser.getEntityElement(mainInstance);
        if (entityElement != null) {
            EntityAction action = EntityFormParser.parseAction(entityElement);
            String dataset = EntityFormParser.parseDataset(entityElement);

            if (action == EntityAction.CREATE) {
                Entity entity = createEntity(entityElement, 1, dataset, saveTos, mainInstance, action);
                formEntryModel.getExtras().put(new EntitiesExtra(asList(entity)));
            } else if (action == EntityAction.UPDATE) {
                int baseVersion = EntityFormParser.parseBaseVersion(entityElement);
                int newVersion = baseVersion + 1;
                Entity entity = createEntity(entityElement, newVersion, dataset, saveTos, mainInstance, action);
                formEntryModel.getExtras().put(new EntitiesExtra(asList(entity)));
            } else {
                formEntryModel.getExtras().put(new EntitiesExtra(emptyList()));
            }
        }
    }

    private Entity createEntity(TreeElement entityElement, int version, String dataset, List<Pair<XPathReference, String>> saveTos, FormInstance mainInstance, EntityAction action) {
        List<Pair<String, String>> fields = saveTos.stream().map(saveTo -> {
            IDataReference reference = saveTo.getFirst();
            IAnswerData answerData = mainInstance.resolveReference(reference).getValue();

            if (answerData != null) {
                return new Pair<>(saveTo.getSecond(), answerData.getDisplayText());
            } else {
                return new Pair<>(saveTo.getSecond(), "");
            }
        }).collect(Collectors.toList());

        String id = EntityFormParser.parseId(entityElement);
        String label = EntityFormParser.parseLabel(entityElement);
        return new Entity(action, dataset, id, label, version, fields);
    }
}
