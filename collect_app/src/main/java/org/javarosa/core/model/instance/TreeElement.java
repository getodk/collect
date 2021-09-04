/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.javarosa.core.model.instance;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.condition.Constraint;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.utils.CompactInstanceWrapper;
import org.javarosa.core.model.instance.utils.DefaultAnswerResolver;
import org.javarosa.core.model.instance.utils.IAnswerResolver;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.instance.utils.TreeElementChildrenList;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.util.DataUtil;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStringLiteral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>An element of a FormInstance.</p>
 *
 * <p>TreeElements represent an XML node in the instance. It may either have a value (e.g., <name>Drew</name>),
 * a number of TreeElement children (e.g., <device /><timestamp /><user_id />), or neither (e.g.,<empty_node />)</p>
 *
 * <p>TreeElements can also represent attributes. Attributes are unique from normal elements in that they are
 * not "children" of their parent, and are always leaf nodes: IE cannot have children.</p>
 *
 * <p>TODO: Split out the bind-able session data from this class and leave only the mandatory values to speed up
 * new DOM-like models</p>
 *
 * @author Clayton Sims
 */

public class TreeElement implements Externalizable, AbstractTreeElement<TreeElement> {
    private String name; // can be null only for hidden root node
    protected int multiplicity = -1; // see TreeReference for special values
    private AbstractTreeElement parent;

    private IAnswerData value;

    private List<FormElementStateListener> observers;
    private List<TreeElement> attributes;
    private final TreeElementChildrenList children = new TreeElementChildrenList();

    /* model properties */
    protected int dataType = Constants.DATATYPE_NULL; //TODO

    private Constraint constraint;
    private String preloadHandler;
    private String preloadParams;
    private List<TreeElement> bindAttributes = new ArrayList<>(0);

    // TODO see what’s required here from commented-out code removed 2017-04-23

    private static final int MASK_REQUIRED = 0x01;
    private static final int MASK_REPEATABLE = 0x02;
    private static final int MASK_ATTRIBUTE = 0x04;
    private static final int MASK_RELEVANT = 0x08;
    private static final int MASK_ENABLED = 0x10;
    private static final int MASK_RELEVANT_INH = 0x20;
    private static final int MASK_ENABLED_INH = 0x40;

    private int flags = MASK_RELEVANT | MASK_ENABLED | MASK_RELEVANT_INH | MASK_ENABLED_INH;

    private String namespace;
    private String namespacePrefix;

    /**
     * The name of the instance that this node is in. The value of the id attribute in the case of a secondary
     * instance or null for the primary instance.
     */
    private String instanceName;

    /**
     * TreeElement with null name and 0 multiplicity? (a "hidden root" node?)
     */
    public TreeElement() {
        this(null, TreeReference.DEFAULT_MULTIPLICITY);
    }

    public TreeElement(String name) {
        this(name, TreeReference.DEFAULT_MULTIPLICITY);
    }

    public TreeElement(String name, int multiplicity) {
        this.name = name;
        this.multiplicity = multiplicity;
        this.parent = null;
        attributes = new ArrayList<>(0);
    }

    /**
     * Construct a TreeElement which represents an attribute with the provided
     * namespace and name.
     *
     * @param namespace - if null will be converted to empty string
     * @param name      a
     * @param value     b
     * @return A new instance of a TreeElement
     */
    public static TreeElement constructAttributeElement(String namespace, String name, String value) {
        TreeElement element = new TreeElement(name);
        element.setIsAttribute(true);
        element.namespace = (namespace == null) ? "" : namespace;
        element.multiplicity = TreeReference.INDEX_ATTRIBUTE;
        element.value = new UncastData(value);
        return element;
    }

    private void setIsAttribute(boolean attribute) {
        setMaskVar(MASK_ATTRIBUTE, attribute);
    }

    /**
     * <p>Retrieves the TreeElement representing the attribute for
     * the provided namespace and name, or null if none exists.</p>
     *
     * <p>If 'null' is provided for the namespace, it will match the first
     * attribute with the matching name.</p>
     *
     * @param attributes - list of attributes to search
     * @param namespace  a
     * @param name       b
     * @return TreeElement
     */
    public static TreeElement getAttribute(List<TreeElement> attributes, String namespace, String name) {
        for (TreeElement attribute : attributes) {
            if (attribute.getName().equals(name) && (namespace == null || namespace.equals(attribute.namespace))) {
                return attribute;
            }
        }
        return null;
    }

    @Override
    public TreeElement getAttribute(String namespace, String name) {
        return getAttribute(attributes, namespace, name);
    }

    public void setAttribute(String namespace, String name, String value) {
        setAttribute(this, attributes, namespace, name, value);
    }

    public static void setAttribute(TreeElement parent, List<TreeElement> attrs, String namespace, String name, String value) {

        TreeElement attribut = getAttribute(attrs, namespace, name);
        if (attribut != null) {
            if (value == null) {
                attrs.remove(attribut);
            } else {
                attribut.setValue(new UncastData(value));
            }
            return;
        }

        // null-valued attributes are a "remove-this" instruction... ignore them
        if (value == null) {
            return;
        }

        // create an attribute...
        TreeElement attr = TreeElement.constructAttributeElement(namespace, name, value);
        attr.setParent(parent);

        attrs.add(attr);
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public boolean isChildable() {
        return value == null;
    }

    @Override
    public String getInstanceName() {
        //CTS: I think this is a better way to do this, although I really, really don't like the duplicated code
        if (parent != null) {
            return parent.getInstanceName();
        }
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setValue(IAnswerData value) {
        if (isLeaf()) {
            this.value = value;
        } else {
            throw new RuntimeException("Can't set data value for node that has children!");
        }
    }

    @Override
    public TreeElement getChild(String name, int multiplicity) {
        return children.get(name, multiplicity);
    }

    @Override
    public List<TreeElement> getChildrenWithName(String name) {
        return children.get(name);
    }

    private int getNumChildrenWithName(String name) {
        return children.getCount(name);
    }

    @Override
    public int getNumChildren() {
        return children.size();
    }

    @Override
    public boolean hasChildren() {
        return getNumChildren() > 0;
    }

    @Override
    public TreeElement getChildAt(int i) {
        return children.get(i);
    }

    @Override
    public boolean isRepeatable() {
        return getMaskVar(MASK_REPEATABLE);
    }

    @Override
    public boolean isAttribute() {
        return getMaskVar(MASK_ATTRIBUTE);
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public void addChild(TreeElement child) {
        if (!isChildable()) {
            throw new RuntimeException("Can't add children to node that has data value!");
        }

        if (child.multiplicity == TreeReference.INDEX_UNBOUND) {
            throw new RuntimeException("Cannot add child with an unbound index!");
        }

        children.addInOrder(child);
        child.setParent(this);
        child.setRelevant(isRelevant(), true);
        child.setEnabled(isEnabled(), true);
        child.setInstanceName(getInstanceName());
    }

    public void removeChild(TreeElement child) {
        children.remove(child);
    }

    public void removeChild(String name, int multiplicity) {
        children.remove(name, multiplicity);
    }

    public void removeChildren(String name) {
        children.removeAll(name);
    }

    public void removeChildAt(int i) {
        children.remove(i);
    }

    @Override
    public int getChildMultiplicity(String name) {
        return getNumChildrenWithName(name);
    }

    public TreeElement shallowCopy() {
        TreeElement newNode = new TreeElement(name, multiplicity);
        newNode.parent = parent;
        newNode.setRepeatable(this.isRepeatable());
        newNode.dataType = dataType;

        // Just set the flag? side effects?
        newNode.setMaskVar(MASK_RELEVANT, this.getMaskVar(MASK_RELEVANT));
        newNode.setMaskVar(MASK_REQUIRED, this.getMaskVar(MASK_REQUIRED));
        newNode.setMaskVar(MASK_ENABLED, this.getMaskVar(MASK_ENABLED));

        newNode.constraint = constraint;
        newNode.preloadHandler = preloadHandler;
        newNode.preloadParams = preloadParams;
        newNode.instanceName = instanceName;
        newNode.namespace = namespace;
        newNode.bindAttributes = bindAttributes;

        newNode.attributes = new ArrayList<>(attributes.size());
        for (TreeElement attr : attributes) {
            newNode.setAttribute(attr.getNamespace(), attr.getName(), attr.getAttributeValue());
        }

        if (value != null) {
            newNode.value = value.clone();
        }

        newNode.children.addAll(children);
        return newNode;
    }

    public TreeElement deepCopy(boolean includeTemplates) {
        TreeElement newNode = shallowCopy();

        newNode.children.clear();
        for (TreeElement child : children) {
            if (includeTemplates || child.getMult() != TreeReference.INDEX_TEMPLATE) {
                newNode.addChild(child.deepCopy(includeTemplates));
            }
        }

        return newNode;
    }

    //For #4059
    public TreeElement deepCopyForRepeat() {
        TreeElement newNode = shallowCopy();

        newNode.children.clear();
        for (TreeElement child : children) {
            //Test that multiplicities must be adjusted
            boolean keepTemplateMultiplicities = false;
            //#4059
            if (!keepTemplateMultiplicities) {
                child.setMult(TreeReference.DEFAULT_MULTIPLICITY);
            }
            newNode.addChild(child.deepCopyForRepeat());
        }
        return newNode;
    }

    /* ==== MODEL PROPERTIES ==== */

    // factoring inheritance rules
    @Override
    public boolean isRelevant() {
        return getMaskVar(MASK_RELEVANT_INH) && getMaskVar(MASK_RELEVANT);
    }

    // factoring in inheritance rules
    public boolean isEnabled() {
        return getMaskVar(MASK_ENABLED_INH) && getMaskVar(MASK_ENABLED);
    }

    /* ==== SPECIAL SETTERS (SETTERS WITH SIDE EFFECTS) ==== */

    public boolean setAnswer(IAnswerData answer) {
        if (value != null || answer != null) {
            setValue(answer);
            alertStateObservers(FormElementStateListener.CHANGE_DATA);
            return true;
        } else {
            return false;
        }
    }

    public void setRequired(boolean required) {
        if (getMaskVar(MASK_REQUIRED) != required) {
            setMaskVar(MASK_REQUIRED, required);
            alertStateObservers(FormElementStateListener.CHANGE_REQUIRED);
        }
    }

    private boolean getMaskVar(int mask) {
        return (flags & mask) == mask;
    }

    private void setMaskVar(int mask, boolean value) {
        if (value) {
            flags = flags | mask;
        } else {
            flags = flags & (Integer.MAX_VALUE - mask);
        }
    }

    public void setRelevant(boolean relevant) {
        setRelevant(relevant, false);
    }

    private void setRelevant(boolean relevant, boolean inherited) {
        boolean oldRelevancy = isRelevant();
        if (inherited) {
            setMaskVar(MASK_RELEVANT_INH, relevant);
        } else {
            setMaskVar(MASK_RELEVANT, relevant);
        }

        boolean newRelevant = isRelevant();
        if (newRelevant != oldRelevancy) {
            if (attributes != null) {
                for (TreeElement attribute : attributes) {
                    attribute.setRelevant(newRelevant, true);
                }
            }
            for (TreeElement child : children) {
                child.setRelevant(newRelevant, true);
            }
            alertStateObservers(FormElementStateListener.CHANGE_RELEVANT);
        }
    }

    public void setBindAttributes(List<TreeElement> bindAttributes) {
        // create new tree elements for all the bind definitions...
        for (TreeElement ref : bindAttributes) {
            setBindAttribute(ref.getNamespace(), ref.getName(), ref.getAttributeValue());
        }
    }

    public List<TreeElement> getBindAttributes() {
        return bindAttributes;
    }

    /**
     * <p>Retrieves the TreeElement representing an arbitrary bind attribute
     * for this element at the provided namespace and name, or null if none exists.</p>
     *
     * <p>If 'null' is provided for the namespace, it will match the first
     * attribute with the matching name.</p>
     *
     * @return TreeElement
     */
    public TreeElement getBindAttribute(String namespace, String name) {
        return getAttribute(bindAttributes, namespace, name);
    }

    /**
     * Returns the value of the given bind attribute from the given namespace.
     * <p>
     * Example:
     * <ul>
     * <li>Given a binding {@code <bind assertion="blah"/>}, calling {@code getBindAttributeValue(null, "assertion")} will return {@code "blah"}
     * <li>Given a binding {@code <bind odk:assertion="blah"/>}, calling {@code getBindAttributeValue("odk", "assertion")} will return {@code "blah"}
     * </ul>
     * </p>
     *
     * @param namespace the namespace of the attribute. It can be null
     * @param name      the name of the attribute
     */
    public String getBindAttributeValue(String namespace, String name) {
        TreeElement element = getBindAttribute(namespace, name);
        return element == null ? null : getAttributeValue(element);
    }

    public void setBindAttribute(String namespace, String name, String value) {
        setAttribute(this, bindAttributes, namespace, name, value);
    }

    public void setEnabled(boolean enabled) {
        setEnabled(enabled, false);
    }

    public void setEnabled(boolean enabled, boolean inherited) {
        boolean oldEnabled = isEnabled();
        if (inherited) {
            setMaskVar(MASK_ENABLED_INH, enabled);
        } else {
            setMaskVar(MASK_ENABLED, enabled);
        }

        if (isEnabled() != oldEnabled) {
            if (children != null) {
                for (TreeElement child : children) {
                    child.setEnabled(isEnabled(), true);
                }
            }
            alertStateObservers(FormElementStateListener.CHANGE_ENABLED);
        }
    }

    /* ==== OBSERVER PATTERN ==== */

    public void registerStateObserver(FormElementStateListener qsl) {
        if (observers == null) {
            observers = new ArrayList<>(1);
        }

        if (!observers.contains(qsl)) {
            observers.add(qsl);
        }
    }

    public void unregisterStateObserver(FormElementStateListener qsl) {
        if (observers != null) {
            observers.remove(qsl);
            if (observers.isEmpty()) {
                observers = null;
            }
        }
    }

    public void unregisterAll() {
        observers = null;
    }

    public void alertStateObservers(int changeFlags) {
        if (observers != null) {
            for (FormElementStateListener observer : observers) {
                observer.formElementStateChanged(this, changeFlags);
            }
        }
    }

    /* ==== VISITOR PATTERN ==== */

    @Override
    public void accept(ITreeVisitor visitor) {
        visitor.visit(this);

        for (TreeElement child : children) {
            child.accept(visitor);
        }
    }

    /* ==== Attributes ==== */

    @Override
    public int getAttributeCount() {
        return attributes == null ? 0 : attributes.size();
    }

    @Override
    public String getAttributeNamespace(int index) {
        return attributes.get(index).namespace;
    }

    @Override
    public String getAttributeName(int index) {
        return attributes.get(index).name;
    }

    @Override
    public String getAttributeValue(int index) {
        return getAttributeValue(attributes.get(index));
    }

    /**
     * Get the String value of the provided attribute
     *
     * @param attribute a
     */
    private String getAttributeValue(TreeElement attribute) {
        if (attribute.getValue() == null) {
            return null;
        } else {
            return attribute.getValue().uncast().getString();
        }
    }

    @Override
    public String getAttributeValue(String namespace, String name) {
        TreeElement element = getAttribute(namespace, name);
        return element == null ? null : getAttributeValue(element);
    }

    public String getAttributeValue() {
        if (!isAttribute()) {
            throw new IllegalStateException("this is not an attribute");
        }
        return getValue().uncast().getString();
    }

    /* ==== SERIALIZATION ==== */

    /*
     * TODO:
     *
     * This new serialization scheme is kind of lame. ideally, we shouldn't have
     * to sub-class TreeElement at all; we should have an API that can
     * seamlessly represent complex data model objects (like weight history or
     * immunizations) as if they were explicity XML subtrees underneath the
     * parent TreeElement.
     *
     * Failing that, we should wrap this scheme in an ExternalizableWrapper.
     */

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        name = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        multiplicity = ExtUtil.readInt(in);
        flags = ExtUtil.readInt(in);
        value = (IAnswerData) ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);

        // children = ExtUtil.nullIfEmpty((List)ExtUtil.read(in, new
        // ExtWrapList(TreeElement.class), pf));

        // Jan 22, 2009 - csims@dimagi.com
        // old line: children = ExtUtil.nullIfEmpty((List)ExtUtil.read(in, new
        // ExtWrapList(TreeElement.class), pf));
        // New Child deserialization
        // 1. read null status as boolean
        // 2. read number of children
        // 3. for i < number of children
        // 3.1 if read boolean true , then create TreeElement and deserialize
        // directly.
        // 3.2 if read boolean false then create tagged element and deserialize
        // child
        if (!ExtUtil.readBool(in)) {
            // 1.
            children.clear(); // todo is this needed?
        } else {
            // 2.
            int numChildren = (int) ExtUtil.readNumeric(in);
            children.clear();
            // 3.
            List<TreeElement> newChildren = new ArrayList<>(numChildren);
            for (int i = 0; i < numChildren; ++i) {
                boolean normal = ExtUtil.readBool(in);
                TreeElement child;

                if (normal) {
                    // 3.1
                    child = new TreeElement();
                    child.readExternal(in, pf);
                } else {
                    // 3.2
                    child = (TreeElement) ExtUtil.read(in, new ExtWrapTagged(), pf);
                }
                child.setParent(this);
                newChildren.add(child);
            }
            children.addAll(newChildren);
        }

        // end Jan 22, 2009

        dataType = ExtUtil.readInt(in);
        instanceName = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        constraint = (Constraint) ExtUtil.read(in, new ExtWrapNullable(
                Constraint.class), pf);
        preloadHandler = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        preloadParams = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        namespace = ExtUtil.nullIfEmpty(ExtUtil.readString(in));

        bindAttributes = ExtUtil.readAttributes(in, this);

        attributes = ExtUtil.readAttributes(in, this);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(name));
        ExtUtil.writeNumeric(out, multiplicity);
        ExtUtil.writeNumeric(out, flags);
        ExtUtil.write(out, new ExtWrapNullable(value == null ? null : new ExtWrapTagged(value)));

        // Jan 22, 2009 - csims@dimagi.com
        // old line: ExtUtil.write(out, new
        // ExtWrapList(ExtUtil.emptyIfNull(children)));
        // New Child serialization
        // 1. write null status as boolean
        // 2. write number of children
        // 3. for all child in children
        // 3.1 if child type == TreeElement write boolean true , then serialize
        // directly.
        // 3.2 if child type != TreeElement, write boolean false, then tagged
        // child
        if (children.isEmpty()) {
            // 1.
            ExtUtil.writeBool(out, false);
        } else {
            // 1.
            ExtUtil.writeBool(out, true);
            // 2.
            ExtUtil.writeNumeric(out, children.size());
            // 3.
            for (TreeElement child : children) {
                if (child.getClass() == TreeElement.class) {
                    // 3.1
                    ExtUtil.writeBool(out, true);
                    child.writeExternal(out);
                } else {
                    // 3.2
                    ExtUtil.writeBool(out, false);
                    ExtUtil.write(out, new ExtWrapTagged(child));
                }
            }
        }

        // end Jan 22, 2009

        ExtUtil.writeNumeric(out, dataType);
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(instanceName));
        ExtUtil.write(out, new ExtWrapNullable(constraint)); // TODO: inefficient for repeats
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(preloadHandler));
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(preloadParams));
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(namespace));

        ExtUtil.writeAttributes(out, bindAttributes);

        ExtUtil.writeAttributes(out, attributes);
    }

    //rebuilding a node from an imported instance
    //  there's a lot of error checking we could do on the received instance, but it's
    //  easier to just ignore the parts that are incorrect
    public void populate(TreeElement incoming, FormDef f) {
        if (this.isLeaf()) {
            // check that incoming doesn't have children?

            IAnswerData value = incoming.getValue();
            if (value == null) {
                this.setValue(null);
            } else if (this.dataType == Constants.DATATYPE_TEXT
                    || this.dataType == Constants.DATATYPE_NULL) {
                this.setValue(value); // value is a StringData
            } else {
                String textVal = (String) value.getValue();

                // if there is no other IAnswerResolver, use the default one.
                IAnswerResolver answerResolver = XFormParser.getAnswerResolver();
                if (answerResolver == null) {
                    answerResolver = new DefaultAnswerResolver();
                }
                this.setValue(answerResolver.resolveAnswer(textVal, this, f));
            }
        } else {
            List<String> names = new ArrayList<>(this.getNumChildren());
            for (int i = 0; i < this.getNumChildren(); i++) {
                TreeElement child = this.getChildAt(i);
                if (!names.contains(child.getName())) {
                    names.add(child.getName());
                }
            }

            // remove all default repetitions from skeleton data model (_preserving_ templates, though)
            for (int i = 0; i < this.getNumChildren(); i++) {
                TreeElement child = this.getChildAt(i);
                if (child.getMaskVar(MASK_REPEATABLE) && child.getMult() != TreeReference.INDEX_TEMPLATE) {
                    this.removeChildAt(i);
                    i--;
                }
            }

            // make sure ordering is preserved (needed for compliance with xsd schema)
            if (this.getNumChildren() != names.size()) {
                throw new RuntimeException("sanity check failed");
            }

            for (int i = 0; i < this.getNumChildren(); i++) {
                TreeElement child = this.getChildAt(i);
                String expectedName = names.get(i);

                if (!child.getName().equals(expectedName)) {
                    TreeElement child2 = null;
                    int j;

                    for (j = i + 1; j < this.getNumChildren(); j++) {
                        child2 = this.getChildAt(j);
                        if (child2.getName().equals(expectedName)) {
                            break;
                        }
                    }
                    if (j == this.getNumChildren()) {
                        throw new RuntimeException("sanity check failed");
                    }

                    this.removeChildAt(j);
                    this.children.add(i, child2);
                }
            }

            for (int i = 0; i < this.getNumChildren(); i++) {
                TreeElement child = this.getChildAt(i);
                List<TreeElement> newChildren = incoming.getChildrenWithName(child.getName());

                if (child.getMaskVar(MASK_REPEATABLE)) {
                    for (int k = 0; k < newChildren.size(); k++) {
                        TreeElement newChild = child.deepCopy(true);
                        newChild.setMult(k);
                        this.children.add(i + k + 1, newChild);
                        newChild.populate(newChildren.get(k), f);
                    }
                    i += newChildren.size();
                } else {

                    if (newChildren.isEmpty()) {
                        child.setRelevant(false);
                    } else {
                        child.populate(newChildren.get(0), f);
                    }
                }
            }
        }

        for (int i = 0; i < incoming.getAttributeCount(); i++) {
            String name = incoming.getAttributeName(i);
            String ns = incoming.getAttributeNamespace(i);
            String value = incoming.getAttributeValue(i);

            this.setAttribute(ns, name, value);
        }
    }

    //this method is for copying in the answers to an itemset. the template node of the destination
    //is used for overall structure (including data types), and the itemset source node is used for
    //raw data. note that data may be coerced across types, which may result in type conversion error
    //very similar in structure to populate()
    public void populateTemplate(TreeElement incoming, FormDef f) {
        if (this.isLeaf()) {
            IAnswerData value = incoming.getValue();
            if (value == null) {
                this.setValue(null);
            } else {
                Class classType = CompactInstanceWrapper.classForDataType(this.dataType);

                if (classType == null) {
                    throw new RuntimeException("data type [" + value.getClass().getName() + "] not supported inside itemset");
                } else if (classType.isAssignableFrom(value.getClass()) &&
                        !(value instanceof SelectOneData || value instanceof MultipleItemsData)) {
                    this.setValue(value);
                } else {
                    String textVal = RestoreUtils.xfFact.serializeData(value);
                    IAnswerData typedVal = RestoreUtils.xfFact.parseData(textVal, this.dataType, this.getRef(), f);
                    this.setValue(typedVal);
                }
            }
        } else {
            for (int i = 0; i < this.getNumChildren(); i++) {
                TreeElement child = this.getChildAt(i);
                List<TreeElement> newChildren = incoming.getChildrenWithName(child.getName());

                if (child.getMaskVar(MASK_REPEATABLE)) {
                    for (int k = 0; k < newChildren.size(); k++) {
                        TreeElement template = f.getMainInstance().getTemplate(child.getRef());
                        TreeElement newChild = template.deepCopy(false);
                        newChild.setMult(k);
                        this.children.add(i + k + 1, newChild);
                        newChild.populateTemplate(newChildren.get(k), f);
                    }
                    i += newChildren.size();
                } else {
                    child.populateTemplate(newChildren.get(0), f);
                }
            }
        }
    }

    //TODO: This is probably silly because this object is likely already
    //not thread safe in any way. Also, we should be wrapping all of the
    //setters.
    final TreeReference[] refCache = new TreeReference[1];

    private void expireReferenceCache() {
        synchronized (refCache) {
            refCache[0] = null;
        }
    }

    @Override
    public TreeReference getRef() {
        //TODO: Expire cache somehow;
        synchronized (refCache) {
            if (refCache[0] == null) {
                refCache[0] = TreeElement.buildRef(this);
            }
            return refCache[0];
        }
    }

    public static TreeReference buildRef(AbstractTreeElement elem) {
        TreeReference ref = TreeReference.selfRef();

        while (elem != null) {
            TreeReference step;

            if (elem.getName() != null) {
                step = TreeReference.selfRef();
                step.add(elem.getName(), elem.getMult());
            } else {
                step = TreeReference.rootRef();
                //All TreeElements are part of a consistent tree, so the root should be in the same instance
            }

            step.setInstanceName(elem.getInstanceName());
            if (elem.getInstanceName() != null) {
                // it is a named instance; it should not inherit runtime context...
                step.setContextType(TreeReference.CONTEXT_INSTANCE);
            }

            ref = ref.parent(step);
            elem = elem.getParent();
        }
        return ref;
    }

    @Override
    public int getDepth() {
        return TreeElement.calculateDepth(this);
    }

    public static int calculateDepth(AbstractTreeElement elem) {
        int depth = 0;

        while (elem.getName() != null) {
            depth++;
            elem = elem.getParent();
        }

        return depth;
    }

    public String getPreloadHandler() {
        return preloadHandler;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setPreloadHandler(String preloadHandler) {
        this.preloadHandler = preloadHandler;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    public String getPreloadParams() {
        return preloadParams;
    }

    public void setPreloadParams(String preloadParams) {
        this.preloadParams = preloadParams;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        expireReferenceCache();
        this.name = name;
    }

    @Override
    public int getMult() {
        return multiplicity;
    }

    public void setMult(int multiplicity) {
        expireReferenceCache();
        this.multiplicity = multiplicity;
    }

    public void setParent(AbstractTreeElement parent) {
        expireReferenceCache();
        this.parent = parent;
    }

    @Override
    public AbstractTreeElement getParent() {
        return parent;
    }

    @Override
    public IAnswerData getValue() {
        return value;
    }

    public String toString() {
        String name = "NULL";
        if (this.name != null) {
            name = this.name;
        }

        return name + " - Children: " + Integer.toString(this.children.size());
    }

    @Override
    public int getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return getMaskVar(MASK_REQUIRED);
    }

    public void setRepeatable(boolean repeatable) {
        setMaskVar(MASK_REPEATABLE, repeatable);
    }

    public int getMultiplicity() {
        return multiplicity;
    }

    @Override
    public void clearCaches() {
        expireReferenceCache();
    }

    public void clearChildrenCaches() {
        for (int i = 0; i < this.getNumChildren(); i++) {
            TreeElement child = this.getChildAt(i);
            child.clearCaches();
            child.clearChildrenCaches();
        }
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public List<TreeReference> tryBatchChildFetch(String name, int mult, List<XPathExpression> predicates, EvaluationContext evalContext) {
        //Only do for predicates
        if (mult != TreeReference.INDEX_UNBOUND || predicates == null) {
            return null;
        }

        List<Integer> toRemove = new ArrayList<>();
        List<TreeReference> selectedChildren = null;

        //Lazy init these until we've determined that our predicate is hintable
        HashMap<XPathPathExpr, String> indices = null;
        List<TreeElement> kids = null;

        predicate:
        for (int i = 0; i < predicates.size(); ++i) {
            XPathExpression xpe = predicates.get(i);
            //what we want here is a static evaluation of the expression to see if it consists of evaluating
            //something we index with something static.
            if (xpe instanceof XPathEqExpr) {
                XPathExpression left = ((XPathEqExpr) xpe).a;
                XPathExpression right = ((XPathEqExpr) xpe).b;

                //For now, only cheat when this is a string literal (this basically just means that we're
                //handling attribute based referencing with very reasonable timing, but it's complex otherwise)
                if (left instanceof XPathPathExpr && right instanceof XPathStringLiteral) {

                    //We're lazily initializing this, since it might actually take a while, and we
                    //don't want the overhead if our predicate is too complex anyway
                    if (indices == null) {
                        indices = new HashMap<>();
                        kids = this.getChildrenWithName(name);

                        if (kids.isEmpty()) {
                            return null;
                        }

                        //Anything that we're going to use across elements should be on all of them
                        TreeElement kid = kids.get(0);
                        for (int j = 0; j < kid.getAttributeCount(); ++j) {
                            String attribute = kid.getAttributeName(j);
                            indices.put(XPathReference.getPathExpr("@" + attribute), attribute);
                        }
                    }

                    for (XPathPathExpr expr : indices.keySet()) {
                        if (expr.equals(left)) {
                            String attributeName = indices.get(expr);

                            for (TreeElement kid : kids) {
                                if (kid.getAttributeValue(null, attributeName).equals(((XPathStringLiteral) right).s)) {
                                    if (selectedChildren == null) {
                                        selectedChildren = new ArrayList<>();
                                    }
                                    selectedChildren.add(kid.getRef());
                                }
                            }

                            //Note that this predicate is evaluated and doesn't need to be evaluated in the future.
                            toRemove.add(DataUtil.integer(i));
                            continue predicate;
                        }
                    }
                }
            }
            //There's only one case where we want to keep moving along, and we would have triggered it if it were going to happen,
            //so otherwise, just get outta here.
            break;
        }

        //if we weren't able to evaluate any predicates, signal that.
        if (selectedChildren == null) {
            return null;
        }

        //otherwise, remove all of the predicates we've already evaluated
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            predicates.remove(toRemove.get(i).intValue());
        }

        return selectedChildren;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }
}
