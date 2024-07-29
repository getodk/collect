package org.odk.collect.entities.javarosa.spec;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_BASE_VERSION;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_CREATE;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_DATASET;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_ID;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_UPDATE;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ELEMENT_ENTITY;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ELEMENT_LABEL;

public class EntityFormParser {

    private EntityFormParser() {

    }

    public static String parseDataset(TreeElement entity) {
        return entity.getAttributeValue(null, ATTRIBUTE_DATASET);
    }

    @Nullable
    public static String parseLabel(TreeElement entity) {
        TreeElement labelElement = entity.getFirstChild(ELEMENT_LABEL);

        if (labelElement != null) {
            IAnswerData labelValue = labelElement.getValue();

            if (labelValue != null) {
                return String.valueOf(labelValue.getValue());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Nullable
    public static String parseId(TreeElement entity) {
        return entity.getAttributeValue("", ATTRIBUTE_ID);
    }

    public static Integer parseBaseVersion(TreeElement entity) {
        try {
            return Integer.valueOf(entity.getAttributeValue("", ATTRIBUTE_BASE_VERSION));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Nullable
    public static TreeElement getEntityElement(FormInstance mainInstance) {
        TreeElement root = mainInstance.getRoot();
        TreeElement meta = root.getFirstChild("meta");

        if (meta != null) {
            return meta.getFirstChild(ELEMENT_ENTITY);
        } else {
            return null;
        }
    }

    @Nullable
    public static EntityAction parseAction(@NotNull TreeElement entity) {
        String create = entity.getAttributeValue(null, ATTRIBUTE_CREATE);
        String update = entity.getAttributeValue(null, ATTRIBUTE_UPDATE);

        if (update != null) {
            if (XPathFuncExpr.boolStr(update)) {
                return EntityAction.UPDATE;
            }
        }

        if (create != null) {
            if (XPathFuncExpr.boolStr(create)) {
                return EntityAction.CREATE;
            }
        }

        return null;
    }
}
