package org.odk.collect.entities.javarosa.finalization;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryFinalizationProcessor;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XPathReference;
import org.odk.collect.entities.javarosa.parse.EntityFormExtra;
import org.odk.collect.entities.javarosa.spec.EntityAction;
import org.odk.collect.entities.javarosa.spec.EntityFormParser;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class EntityFormFinalizationProcessor implements FormEntryFinalizationProcessor {

    @Override
    public void processForm(FormEntryModel formEntryModel) {
        FormDef formDef = formEntryModel.getForm();
        FormInstance mainInstance = formDef.getMainInstance();

        EntityFormExtra entityFormExtra = formDef.getExtras().get(EntityFormExtra.class);
        if (entityFormExtra != null) {
            List<Pair<XPathReference, String>> saveTos = entityFormExtra.getSaveTos();

            List<TreeElement> entityElements = EntityFormParser.getEntityElements(mainInstance.getRoot());
            List<FormEntity> entities = new ArrayList<>();
            for (TreeElement entityElement : entityElements) {
                EntityAction action = EntityFormParser.parseAction(entityElement);
                String dataset = EntityFormParser.parseDataset(entityElement);

                if (action == EntityAction.CREATE || action == EntityAction.UPDATE) {
                    FormEntity entity = createEntity(entityElement, dataset, saveTos, mainInstance, action);
                    entities.add(entity);
                }
            }
            formEntryModel.getExtras().put(new EntitiesExtra(entities));
        }
    }

    private FormEntity createEntity(TreeElement entityElement, String dataset, List<Pair<XPathReference, String>> saveTos, FormInstance mainInstance, EntityAction action) {
        ArrayList<Pair<String, String>> fields = new ArrayList<>();
        for (Pair<XPathReference, String> saveTo : saveTos) {
            TreeReference entityBindRef = (TreeReference) saveTo.getFirst().getReference();
            TreeReference entityGroupRef = entityElement.getRef().getParentRef().getParentRef();
            TreeReference entityFieldRef = entityBindRef.contextualize(entityGroupRef);

            TreeElement element = mainInstance.resolveReference(entityFieldRef);
            if (element.isRelevant()) {
                IAnswerData answerData = element.getValue();
                if (answerData != null) {
                    fields.add(new Pair<>(saveTo.getSecond(), answerData.uncast().getString()));
                } else {
                    fields.add(new Pair<>(saveTo.getSecond(), ""));
                }
            }
        }

        String id = EntityFormParser.parseId(entityElement);
        String label = EntityFormParser.parseLabel(entityElement);
        return new FormEntity(action, dataset, id, label, fields);
    }
}
