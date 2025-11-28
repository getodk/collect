package org.odk.collect.entities.javarosa.spec;

import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_CREATE;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_DATASET;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_ID;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_UPDATE;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ELEMENT_ENTITY;
import static org.odk.collect.entities.javarosa.spec.FormEntityElement.ELEMENT_LABEL;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
                return labelValue.uncast().getString();
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

    @NonNull
    public static List<TreeElement> getEntityElements(TreeElement treeElement) {
        List<TreeElement> entityElements = new ArrayList<>();

        int numOfChildren = treeElement.getNumChildren();
        for (int i = 0; i < numOfChildren; i++) {
            TreeElement childTreeElement = treeElement.getChildAt(i);
            if ("meta".equals(childTreeElement.getName())) {
                TreeElement entity = childTreeElement.getFirstChild(ELEMENT_ENTITY);
                if (entity != null) {
                    entityElements.add(entity);
                }
            } else if (childTreeElement.hasChildren()) {
                entityElements.addAll(getEntityElements(childTreeElement));
            }
        }

        return entityElements;
    }

    public static boolean hasEntityElement(TreeElement treeElement) {
        int numOfChildren = treeElement.getNumChildren();
        for (int i = 0; i < numOfChildren; i++) {
            TreeElement childTreeElement = treeElement.getChildAt(i);
            if ("meta".equals(childTreeElement.getName())) {
                return childTreeElement.getFirstChild(ELEMENT_ENTITY) != null;
            } else if (childTreeElement.hasChildren()) {
                if (hasEntityElement(childTreeElement)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public static EntityAction parseAction(@NotNull TreeElement entity) {
        String create = entity.getAttributeValue(null, ATTRIBUTE_CREATE);
        String update = entity.getAttributeValue(null, ATTRIBUTE_UPDATE);

        boolean shouldCreate = false;
        if (create != null) {
            if (XPathFuncExpr.boolStr(create)) {
                shouldCreate = true;
            }
        }

        boolean shouldUpdate = false;
        if (update != null) {
            if (XPathFuncExpr.boolStr(update)) {
                shouldUpdate = true;
            }
        }

        if (shouldCreate && shouldUpdate) {
            return null;
        } else if (shouldCreate) {
            return EntityAction.CREATE;
        } else if (shouldUpdate) {
            return EntityAction.UPDATE;
        } else {
            return null;
        }

    }
}
