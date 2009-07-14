/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.collect.android;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.IXFormyFactory;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.IService;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormAnswerDataParser;
import org.javarosa.xpath.XPathParseTool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Given a {@link FormDef}, enables form iteration.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormHandler {

    public final String t = "FormHandler";

    private FormDef mForm;
    private FormIndex mCurrentIndex;
    private int mQuestionCount;
    private String mSourcePath;
    private Context mContext;


    public FormHandler(FormDef formDef) {
        Log.i(t, "calling constructor");

        mForm = formDef;
        mCurrentIndex = FormIndex.createBeginningOfFormIndex();

    }


    public void registerXFormsModule() {
        String[] classes =
                {"org.javarosa.model.xform.XPathReference", "org.javarosa.xpath.XPathConditional"};

        JavaRosaServiceProvider.instance().registerPrototypes(classes);
        JavaRosaServiceProvider.instance().registerPrototypes(XPathParseTool.xpathClasses);
        RestoreUtils.xfFact = new IXFormyFactory() {
            public TreeReference ref(String refStr) {
                return DataModelTree.unpackReference(new XPathReference(refStr));
            }


            public IDataPayload serializeModel(DataModelTree dm) {
                try {
                    return (new XFormSerializingVisitor()).createSerializedPayload(dm);
                } catch (IOException e) {
                    return null;
                }
            }


            public DataModelTree parseRestore(byte[] data, Class restorableType) {
                return XFormParser.restoreDataModel(data, restorableType);
            }


            public IAnswerData parseData(String textVal, int dataType, TreeReference ref, FormDef f) {
                return XFormAnswerDataParser.getAnswerData(textVal, dataType, XFormParser
                        .ghettoGetQuestionDef(dataType, f, ref));
            }
        };
    }


    public void initialize(Context context) {

        mContext = context;

        registerXFormsModule();


        // load services
        Vector<IService> v = new Vector<IService>();
        v.add(new PropertyManager(context));
        JavaRosaServiceProvider.instance().initialize(v);

        // set evaluation context
        EvaluationContext ec = new EvaluationContext();
        ec.addFunctionHandler(new RegexFunction());
        mForm.setEvaluationContext(ec);

        // initialize form
        mForm.initialize(true);

    }


    /**
     * Attempts to save the answer 'answer' into prompt.
     * 
     * @param prompt
     * @param answer
     */
    public int saveAnswer(PromptElement prompt, IAnswerData answer, boolean validate) {
        if (!mForm.evaluateConstraint(prompt.getInstanceRef(), answer) && validate) {
            return SharedConstants.ANSWER_CONSTRAINT_VIOLATED;
        } else if (prompt.isRequired() && answer == null && validate) {
            return SharedConstants.ANSWER_REQUIRED_BUT_EMPTY;
        } else {
            mForm.setValue(answer, prompt.getInstanceRef(), prompt.getInstanceNode());
            return SharedConstants.ANSWER_OK;
        }
    }


    /**
     * Deletes the innermost group that repeats that this node belongs to.
     */
    public void deleteCurrentRepeat() {
        mCurrentIndex = mForm.deleteRepeat(mCurrentIndex);
    }


    /**
     * Returns a vector of the GroupElement hierarchy to which this question
     * belongs
     */
    private Vector<GroupElement> getGroups() {
        Vector<Integer> mult = new Vector<Integer>();
        Vector<IFormElement> elements = new Vector<IFormElement>();
        Vector<GroupElement> groups = new Vector<GroupElement>();

        getForm().collapseIndex(mCurrentIndex, new Vector<Object>(), mult, elements);
        for (int i = 0; i < elements.size(); i++) {
            IFormElement fi = elements.get(i);
            if (fi instanceof GroupDef) {
                groups.add(new GroupElement(((GroupDef) fi).getLongText(), mult.get(i).intValue(),
                        ((GroupDef) fi).getRepeat()));
            }
        }

        return groups;
    }


    /**
     * Set the currentIndex to the specified FormIndex
     * 
     * @param newIndex
     */
    public void setFormIndex(FormIndex newIndex) {
        mCurrentIndex = newIndex;
    }


    private void createModelIfNecessary(FormIndex index) {
        if (index.isInForm()) {
            IFormElement e = getForm().getChild(index);
            if (e instanceof GroupDef) {
                GroupDef g = (GroupDef) e;
                if (g.getRepeat() && g.getCountReference() != null) {
                    IAnswerData count =
                            getForm().getDataModel().getDataValue(g.getCountReference());
                    if (count != null) {
                        int fullcount = ((Integer) count.getValue()).intValue();
                        TreeReference ref = getForm().getChildInstanceRef(index);
                        TreeElement element = getForm().getDataModel().resolveReference(ref);
                        if (element == null) {
                            if (index.getInstanceIndex() < fullcount) {
                                getForm().createNewRepeat(index);
                            }
                        }
                    }
                }
            }
        }
    }



    public FormIndex getIndex() {
        return mCurrentIndex;
    }


    private boolean isNoAsk(FormIndex index) {
        Vector<IFormElement> defs = getIndexVector(index);
        IFormElement last = (defs.size() == 0 ? null : (IFormElement) defs.lastElement());
        if (last instanceof GroupDef) {
            GroupDef end = (GroupDef) last;
            return end.noAddRemove;
        }
        return false;
    }


    /**
     * Returns the prompt associated with the next relevant question or repeat.
     * For filling out forms, use this rather than nextQuestionPrompt()
     * 
     */
    public PromptElement nextPrompt() {
        nextRelevantIndex();

        /*
         * First see if we need to build a set of repeats. Then Check here to
         * see if the noaskrepeat is set. If it is, then this node would
         * normally trigger a "add repeat?" dialog, so we just skip it.
         */
        createModelIfNecessary(mCurrentIndex);
        if (isNoAsk(mCurrentIndex)) {
            nextRelevantIndex();
        }

        while (!isEnd()) {
            Vector<IFormElement> defs = getIndexVector(mCurrentIndex);
            if (indexIsGroup(mCurrentIndex)) {
                GroupDef last = (defs.size() == 0 ? null : (GroupDef) defs.lastElement());

                if (last.getRepeat() && resolveReferenceForCurrentIndex() == null) {
                    return new PromptElement(getGroups());
                } else {
                    // this group doesn't repeat, so skip passed it
                }
            } else {
                // we have a question
                mQuestionCount++;
                return new PromptElement(mCurrentIndex, getForm(), getGroups());
            }
            nextRelevantIndex();
        }
        mQuestionCount++;
        // we're at the end of our form
        return null;
    }


    /**
     * returns the prompt associated with the next relevant question. This
     * should only be used when iterating through the questions as it ignores
     * any repeats
     * 
     */
    public PromptElement nextQuestionPrompt() {
        nextRelevantIndex();

        while (!isEnd()) {
            if (indexIsGroup(mCurrentIndex)) {
                nextRelevantIndex();
                continue;
            } else {
                // we have a question
                return new PromptElement(mCurrentIndex, getForm(), getGroups());
            }
        }

        return null;
    }


    public PromptElement currentPrompt() {
        if (indexIsGroup(mCurrentIndex))
            return new PromptElement(getGroups());
        else
            return new PromptElement(mCurrentIndex, getForm(), getGroups());
    }


    /**
     * returns the prompt for the previous relevant question
     * 
     */
    public PromptElement prevPrompt() {
        mQuestionCount--;
        prevQuestion();
        if (isBeginning())
            return null;
        else
            return new PromptElement(mCurrentIndex, getForm(), getGroups());
    }


    private void nextRelevantIndex() {
        do {
            mCurrentIndex = mForm.incrementIndex(mCurrentIndex);
        } while (mCurrentIndex.isInForm() && !isRelevant(mCurrentIndex));
    }


    /**
     * moves index to the previous relevant index representing a question,
     * skipping any groups
     */
    private void prevQuestion() {
        do {
            mCurrentIndex = mForm.decrementIndex(mCurrentIndex);
        } while (mCurrentIndex.isInForm() && !isRelevant(mCurrentIndex));

        // recursively skip backwards past any groups, and pop them from our
        // stack
        if (indexIsGroup(mCurrentIndex)) {
            prevQuestion();
        }
    }


    private boolean indexIsGroup(FormIndex index) {
        Vector<IFormElement> defs = getIndexVector(index);
        IFormElement last = (defs.size() == 0 ? null : (IFormElement) defs.lastElement());
        if (last instanceof GroupDef) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isBeginning() {
        if (mCurrentIndex.isBeginningOfFormIndex())
            return true;
        else
            return false;
    }


    public boolean isEnd() {
        if (mCurrentIndex.isEndOfFormIndex())
            return true;
        else
            return false;
    }


    /**
     * returns true if the specified FormIndex should be displayed, false
     * otherwise
     * 
     * @param questionIndex
     */
    private boolean isRelevant(FormIndex questionIndex) {
        TreeReference ref = mForm.getChildInstanceRef(questionIndex);
        boolean isAskNewRepeat = false;

        Vector<IFormElement> defs = getIndexVector(questionIndex);
        IFormElement last = (defs.size() == 0 ? null : (IFormElement) defs.lastElement());
        if (last instanceof GroupDef
                && ((GroupDef) last).getRepeat()
                && mForm.getDataModel().resolveReference(mForm.getChildInstanceRef(questionIndex)) == null) {
            isAskNewRepeat = true;
        }

        boolean relevant;
        if (isAskNewRepeat) {
            relevant = mForm.canCreateRepeat(ref);
        } else {
            TreeElement node = mForm.getDataModel().resolveReference(ref);
            relevant = node.isRelevant(); // check instance flag first
        }

        if (relevant) { // if instance flag/condition says relevant, we still
            // have
            // to check the <group>/<repeat> hierarchy
            FormIndex ancestorIndex = null;
            FormIndex cur = null;
            FormIndex qcur = questionIndex;
            for (int i = 0; i < defs.size() - 1; i++) {
                FormIndex next = new FormIndex(qcur.getLocalIndex(), qcur.getInstanceIndex());
                if (ancestorIndex == null) {
                    ancestorIndex = next;
                    cur = next;
                } else {
                    cur.setNextLevel(next);
                    cur = next;
                }
                qcur = qcur.getNextLevel();

                TreeElement ancestorNode =
                        mForm.getDataModel().resolveReference(
                                mForm.getChildInstanceRef(ancestorIndex));
                if (!ancestorNode.isRelevant()) {
                    relevant = false;
                    break;
                }
            }
        }

        return relevant;
    }


    public void newRepeat() {
        mForm.createNewRepeat(mCurrentIndex);
    }


    public String getCurrentLanguage() {
        if (mForm.getLocalizer() != null) {
            return mForm.getLocalizer().getLocale();
        }
        return null;
    }


    public String[] getLanguages() {
        if (mForm.getLocalizer() != null) {
            return mForm.getLocalizer().getAvailableLocales();
        }
        return null;
    }


    public void setLanguage(String language) {
        if (mForm.getLocalizer() != null) {
            mForm.getLocalizer().setLocale(language);
        }
    }


    @SuppressWarnings("unchecked")
    public Vector<IFormElement> getIndexVector(FormIndex index) {
        return mForm.explodeIndex(index);
    }


    public FormDef getForm() {
        return mForm;
    }


    private TreeElement resolveReferenceForCurrentIndex() {
        return mForm.getDataModel().resolveReference(mForm.getChildInstanceRef(mCurrentIndex));
    }


    public float getQuestionProgress() {
        return mQuestionCount / getQuestionCount();
    }


    public int getQuestionNumber() {
        return mQuestionCount;
    }


    // TODO: These two methods are really hacky and completely inefficient. We
    // should figure out a way to keep
    // track of the number of questions we have without having to loop through
    // the entire index each time.
    public FormIndex nextIndexForCount(FormIndex i) {
        do {
            i = mForm.incrementIndex(i);
        } while (i.isInForm() && !isRelevant(i));
        return i;
    }


    public int getQuestionCount() {
        int count = 0;
        FormIndex i = FormIndex.createBeginningOfFormIndex();

        i = nextIndexForCount(i);

        while (!i.isEndOfFormIndex()) {
            if (!indexIsGroup(i)) {
                // we have a question
                count++;
            }
            i = nextIndexForCount(i);
        }

        // +1 for end screen
        return count + 1;
    }


    public String getFormTitle() {
        return mForm.getTitle();
    }


    public void setSourcePath(String path) {
        mSourcePath = path;
    }


    public String getSourcePath() {
        return mSourcePath;
    }



    /**
     * Runs post processing handlers. Necessary to get end time.
     */
    public void finalizeDataModel() {

        mForm.postProcessModel();

    }



    public void importData() {

        File file =
                new File(
                        "/sdcard/odk/answers/training_form_8_2009-07-13_17-16-55/training_form_8_2009-07-13_17-16-55.xml");

        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            try {
                throw new IOException("Could not completely read file " + file.getName());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Close the input stream and return bytes
        try {
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DataModelTree incoming = XFormParser.restoreDataModel(bytes, null);
        TreeElement incoming_root = incoming.getRoot();
        DataModelTree template = mForm.getDataModel();

        // TreeElement fixedRoot =
        // DataModelTree.processSavedDataModel(brokenTreeRoot, newDataModel,
        // mForm);

        TreeElement fixedRoot = processSavedDataModel(incoming_root, template, mForm);



        DataModelTree fixedTree = new DataModelTree(fixedRoot);
        mForm.setDataModel(fixedTree);

    }


    public static TreeElement processSavedDataModel(TreeElement incoming_root,
            DataModelTree template, FormDef f) {
        TreeElement newModelRoot = template.getRoot().deepCopy(true);

        TreeElement incomingRoot = incoming_root;
        // TreeElement incomingRoot = (TreeElement)
        // incoming_root.getChildren().elementAt(0);

        if (!newModelRoot.getName().equals(incomingRoot.getName())) {
            Log.i("yaw", "names don't match");
            Log.i("yaw", "new model:" + newModelRoot.getName());
            Log.i("yaw", "incoming root:" + incomingRoot.getName());

        }
        if (incomingRoot.getMult() != 0) {
            Log.i("yaw", "incoming root ");

        }

        if (!newModelRoot.getName().equals(incomingRoot.getName()) || incomingRoot.getMult() != 0) {
            throw new RuntimeException(
                    "Saved form instance to restore does not match form definition");
        }
        TreeReference ref = TreeReference.rootRef();
        ref.add(newModelRoot.getName(), TreeReference.INDEX_UNBOUND);

        DataModelTree.populateNode(newModelRoot, incomingRoot, ref, f);

        return newModelRoot;
    }



    /**
     * Loop through the data model and moves binary files into the answer
     * folder. Also replace Android specific URI's with filenames.
     */
    private boolean exportBinaryFiles(String answerPath) {

        Uri u;
        Cursor c;
        String b;

        // move index to the beginning
        FormIndex fi = FormIndex.createBeginningOfFormIndex();
        fi = nextIndexForCount(fi);

        // loop through entire data model.
        while (!fi.isEndOfFormIndex()) {

            if (!indexIsGroup(fi)) {
                // we have a question
                PromptElement pe = new PromptElement(fi, mForm, null);

                // select only binary files with android specific uri
                if (pe.getAnswerType() == Constants.DATATYPE_BINARY && pe.getAnswerObject() != null) {

                    // get uri
                    u = Uri.parse(pe.getAnswerText());
                    c = mContext.getContentResolver().query(u, null, null, null, null);
                    c.moveToFirst();

                    // get the file path and move it to the answer folder
                    File f = new File(c.getString(c.getColumnIndex("_data")));
                    b = c.getString(c.getColumnIndex("_display_name"));
                    boolean move = f.renameTo(new File(answerPath + "/" + b));

                    // is move successful?
                    if (move) {

                        // remove the database entry
                        mContext.getContentResolver().delete(u, null, null);

                        // replace the answer
                        saveAnswer(pe, new StringData(b), true);
                    } else {

                        Log.e(t, "Could not move " + pe.getAnswerText());
                        return false;
                    }

                }
            }

            // next element
            fi = nextIndexForCount(fi);
        }

        return true;
    }


    /**
     * Loop through the data model and writes the XML file into the answer
     * folder.
     */
    private boolean exportXmlFile(ByteArrayPayload payload, String path) {

        // create data stream
        InputStream is = payload.getPayloadStream();
        int len = (int) payload.getLength();

        // read from data stream
        byte[] data = new byte[len];
        try {
            is.read(data, 0, len);
        } catch (IOException e) {
            Log.e(t, "Error reading from payload data stream");
            e.printStackTrace();
            return false;
        }

        // write xml file
        try {
            String filename = path + "/" + path.substring(path.lastIndexOf('/') + 1) + ".xml";
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            bw.write(new String(data, "UTF-8"));
            bw.flush();
            bw.close();
            return true;

        } catch (IOException e) {
            Log.e(t, "Error writing XML file");
            e.printStackTrace();
            return false;
        }

    }


    /**
     * Serialize data model and extract payload. Exports both binaries and xml.
     */
    @SuppressWarnings("unchecked")
    public boolean exportData(String answerPath) {

        ByteArrayPayload payload;

        try {
            // assume no binary data inside the model.
            DataModelTree datamodel = mForm.getDataModel();
            XFormSerializingVisitor serializer = new XFormSerializingVisitor();
            payload = (ByteArrayPayload) serializer.createSerializedPayload(datamodel);
        } catch (IOException e) {
            Log.e(t, "Error creating serialized payload");
            e.printStackTrace();
            return false;
        }

        // write out binary and xml
        if (exportBinaryFiles(answerPath) && exportXmlFile(payload, answerPath)) {
            return true;
        } else {
            return false;
        }
    }


}
