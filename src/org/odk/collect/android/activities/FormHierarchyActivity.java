package org.odk.collect.android.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.formmanager.view.FormElementBinding;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.logic.FormHandler;
import org.odk.collect.android.logic.HierarchyElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * 
 * TODO: WARNING, this file is by no means complete, or correct for that matter.
 * It is hacky attempt #1 to make a hierarchy viewer by stealing a bunch of
 * things from formHandler. JavaRosa should give us better methods to accomplish
 * this (and will in the future...fingers crossed) But until then, we're
 * trying...
 * 
 */
public class FormHierarchyActivity extends ListActivity {

    private static final String t = "FormHierarchyActivity";
    FormDef mForm;
    int state;

    private static final int CHILD = 1;
    private static final int EXPANDED = 2;
    private static final int COLLAPSED = 3;
    private static final int QUESTION = 4;

    private final String mIndent = "     ";

    private Button mBackButton;

    private FormIndex mCurrentIndex;
    List<HierarchyElement> formList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hierarchy_layout);


        // We'll use formhandler to set the CurrentIndex before returning to
        // FormEntryActivity
        FormHandler mFormHandler = FormEntryActivity.mFormHandler;

        mForm = mFormHandler.getForm();

        setTitle(getString(R.string.app_name) + " > " + mFormHandler.getFormTitle());


        mCurrentIndex = mFormHandler.getIndex();
        if (mCurrentIndex.isBeginningOfFormIndex()) {
            // we always have to start on a valid formIndex
            mCurrentIndex = mForm.incrementIndex(mCurrentIndex);
        }

        mBackButton = (Button) findViewById(R.id.backbutton);
        mBackButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.e("carl", "clicked back");
                mCurrentIndex = stepIndexOut(mCurrentIndex);
                /*
                 * Log.e("carl", "mCurrentIndex = " + mCurrentIndex);
                 * Log.e("Carl", "local index = " +
                 * mCurrentIndex.getLocalIndex()); Log.e("carl",
                 * "instance index = " + mCurrentIndex.getInstanceIndex());
                 */

                if (mCurrentIndex == null || indexIsBeginning(mCurrentIndex)) {
                    mCurrentIndex = FormIndex.createBeginningOfFormIndex();
                    mCurrentIndex = mForm.incrementIndex(mCurrentIndex);
                } else {

                    FormIndex levelTest = mCurrentIndex;
                    int level = 0;
                    while (levelTest.getNextLevel() != null) {
                        level++;
                        levelTest = levelTest.getNextLevel();
                    }

                    FormIndex tempIndex;
                    boolean done = false;
                    while (!done) {
                        Log.e("carl", "stepping in loop");
                        tempIndex = mCurrentIndex;
                        int i = 0;
                        while (tempIndex.getNextLevel() != null && i < level) {
                            Log.e("carl", "stepping in next level");
                            tempIndex = tempIndex.getNextLevel();
                            i++;
                        }
                        if (tempIndex.getLocalIndex() == 0) {
                            done = true;
                        } else {
                            mCurrentIndex = prevIndex(mCurrentIndex);
                        }
                        // Log.e("carl", "temp instance = " +
                        // tempIndex.getInstanceIndex());
                    }
                    Log.e("carl", "now showing : " + mCurrentIndex);
                    Log
                            .e("Carl", "now shoing instance index = "
                                    + mCurrentIndex.getInstanceIndex());
                }
                refreshView();

                // we've already stepped back...
                // now we just have to refresh, I think
            }
        });


        Button jumpButton = (Button) findViewById(R.id.jumpbutton);
        jumpButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                createJumpDialog();
            }
        });
        refreshView();

    }


    private void createJumpDialog() {
        final String[] items = {getString(R.string.jump_to_start), getString(R.string.jump_to_end)};
        AlertDialog mJumpDialog =
                new AlertDialog.Builder(this).setTitle(R.string.jump_to).setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (items[which].equals(getString(R.string.jump_to_start))) {
                                    mCurrentIndex = FormIndex.createBeginningOfFormIndex();
                                    FormEntryActivity.mFormHandler.setFormIndex(mCurrentIndex);
                                    finish();
                                } else {
                                    mCurrentIndex = FormIndex.createEndOfFormIndex();
                                    FormEntryActivity.mFormHandler.setFormIndex(mCurrentIndex);
                                    finish();
                                }
                            }
                        }).create();
        mJumpDialog.show();


    }


    private boolean indexIsBeginning(FormIndex fi) {
        String startTest = fi.toString();
        int firstComma = startTest.indexOf(",");
        Log.e("carl", "firstcomma found at " + firstComma);
        int secondComma = startTest.indexOf(",", firstComma + 1);
        Log.e("carl", "secondcomma found at " + secondComma);
        boolean beginning = (secondComma == -1);
        return beginning;
    }


    public void refreshView() {
        formList = new ArrayList<HierarchyElement>();

        FormIndex currentIndex = mCurrentIndex;

        // would like to use this, but it's broken and changes the currentIndex
        // TODO: fix in javarosa.
        /*
         * FormIndex startTest = stepIndexOut(currentIndex); Log.e("carl",
         * "starttest = " + startTest); boolean beginning = (startTest == null);
         */

        // begin hack around:
        boolean beginning = indexIsBeginning(currentIndex);
        // end hack around

        String displayGroup = "";
        int level = 0;
        String repeatGroup = "-1";

        Log.e("Carl", "just checking at beginnig " + currentIndex);
        if (!beginning) {
            FormIndex levelTest = currentIndex;
            while (levelTest.getNextLevel() != null) {
                level++;
                levelTest = levelTest.getNextLevel();
            }
            Log.e("Carl", "level is: " + level);

            boolean found = false;
            while (!found) {
                FormIndex localTest = currentIndex;
                for (int i = 0; i < level; i++) {
                    localTest = localTest.getNextLevel();
                }
                Log.e("carl", "localtest local = " + localTest.getLocalIndex() + " and instance = "
                        + localTest.getInstanceIndex());
                if (localTest.getLocalIndex() == 0)
                    found = true;
                else
                    currentIndex = prevIndex(currentIndex);
            }

            // we're displaying only things only within a given group
            FormIndex prevIndex = prevIndex(currentIndex);
            Log.e("Carl", "local = " + prevIndex.getLocalIndex() + " and instance = "
                    + prevIndex.getInstanceIndex());
            displayGroup = mForm.getChildInstanceRef(prevIndex).toString(false);
            Log.e("carl", "display group is: " + displayGroup);

            mBackButton.setEnabled(true);

        } else {
            Log.e("carl", "at beginning");
            currentIndex = FormIndex.createBeginningOfFormIndex();
            currentIndex = nextRelevantIndex(currentIndex);
            Log
                    .e("carl", "index is now "
                            + mForm.getChildInstanceRef(currentIndex).toString(false));
            Log.e("carl", "index # is " + currentIndex);
            mBackButton.setEnabled(false);
        }

        int repeatIndex = -1;
        int groupCount = 1;
        String repeatedGroupName = "";
        while (!isEnd(currentIndex)) {
            FormIndex normalizedLevel = currentIndex;
            for (int i = 0; i < level; i++) {
                normalizedLevel = normalizedLevel.getNextLevel();
                Log.e("carl", "incrementing normalized level");
            }
            Log.e("carl", "index # is: " + currentIndex + " for: "
                    + mForm.getChildInstanceRef(currentIndex).toString(false));

            IFormElement e = mForm.getChild(currentIndex);
            String currentGroupName = mForm.getChildInstanceRef(currentIndex).toString(false);

            // we're displaying only a particular group, and we've reached the
            // end of that group
            if (displayGroup.equalsIgnoreCase(currentGroupName)) {
                break;
            }

            // Here we're adding new child elements to a group, or skipping over
            // elements in the index
            // that are just members of the current group.
            if (currentGroupName.startsWith(repeatGroup)) {
                Log.e("carl", "testing: " + repeatIndex + " against: "
                        + normalizedLevel.getInstanceIndex());

                // the last repeated group doesn't exist, so make sure the next
                // item is still in the group.
                FormIndex nextIndex = nextRelevantIndex(currentIndex);
                if (nextIndex.isEndOfFormIndex()) break;
                String nextIndexName = mForm.getChildInstanceRef(nextIndex).toString(false);
                if (repeatIndex != normalizedLevel.getInstanceIndex()
                        && nextIndexName.startsWith(repeatGroup)) {

                    repeatIndex = normalizedLevel.getInstanceIndex();
                    Log.e("Carl", "adding new group: " + currentGroupName + " in repeat");
                    HierarchyElement h = formList.get(formList.size() - 1);
                    h.AddChild(new HierarchyElement(mIndent + repeatedGroupName + " "
                            + groupCount++, "", null, Color.WHITE, CHILD, currentIndex));
                }

                // if it's not a new repeat, we skip it because it's in the
                // group anyway
                currentIndex = nextRelevantIndex(currentIndex);
                continue;
            }

            if (e instanceof GroupDef) {
                GroupDef g = (GroupDef) e;
                // h += "\t" + g.getLongText() + "\t" + g.getRepeat();

                if (g.getRepeat() && !currentGroupName.startsWith(repeatGroup)) {

                    // we have a new repeated group that we haven't seen
                    // before
                    repeatGroup = currentGroupName;
                    repeatIndex = normalizedLevel.getInstanceIndex();
                    Log.e("carl", "found new repeat: " + repeatGroup + " with instance index: "
                            + repeatIndex);

                    FormIndex nextIndex = nextRelevantIndex(currentIndex);
                    if (nextIndex.isEndOfFormIndex()) break;
                    String nextIndexName = mForm.getChildInstanceRef(nextIndex).toString(false);
                    // Make sure the next element is in this group, else no
                    // reason to add it
                    if (nextIndexName.startsWith(repeatGroup)) {
                        groupCount = 1;
                        // add the group, but this index is also the first
                        // instance of a
                        // repeat, so add it as a child of the group
                        repeatedGroupName = g.getLongText();
                        HierarchyElement group =
                                new HierarchyElement(repeatedGroupName,
                                        getString(R.string.collapsed_group), getResources()
                                                .getDrawable(R.drawable.expander_ic_minimized),
                                        Color.WHITE, COLLAPSED, currentIndex);
                        group.AddChild(new HierarchyElement(mIndent + repeatedGroupName + " "
                                + groupCount++, "", null, Color.WHITE, CHILD, currentIndex));
                        formList.add(group);
                    } else {
                        Log.e("Carl", "no children, so skipping");
                    }
                    currentIndex = nextRelevantIndex(currentIndex);
                    continue;

                }
            } else if (e instanceof QuestionDef) {
                QuestionDef q = (QuestionDef) e;
                // h += "\t" + q.getLongText();
                // Log.e("FHV", h);
                String answer = "";
                FormElementBinding feb = new FormElementBinding(null, currentIndex, mForm);
                IAnswerData a = feb.getValue();
                if (a != null) {
                    if (feb.instanceNode.dataType == Constants.DATATYPE_DATE) {
                        answer = new SimpleDateFormat("MMM dd, yyyy").format((Date) ((DateData) a).getValue());
                    } else {
                        answer = a.getDisplayText();
                    }
                }

                String questionText = feb.form.fillTemplateString(q.getLongText(), feb.instanceRef);

                formList.add(new HierarchyElement(questionText, answer, null, Color.WHITE,
                        QUESTION, currentIndex));
            } else {
                Log.e(t, "we shouldn't get here");
            }

            currentIndex = nextRelevantIndex(currentIndex);
        }

        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);

    }


    // used to go 'back', the only problem is this changes whatever it's
    // referencing
    public FormIndex stepIndexOut(FormIndex index) {
        if (index.isTerminal()) {
            return null;
        } else {
            index.setNextLevel(stepIndexOut(index.getNextLevel()));
            return index;
        }
    }


    private FormIndex prevIndex(FormIndex index) {
        do {
            index = mForm.decrementIndex(index);
        } while (index.isInForm() && !isRelevant(index));
        return index;
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HierarchyElement h = (HierarchyElement) l.getItemAtPosition(position);
        Log.e("carl", "item is " + h.getPrimaryText());

        switch (h.getType()) {
            case EXPANDED:
                Log.e("carl", "is expanded group, collapsing");
                h.setType(COLLAPSED);
                ArrayList<HierarchyElement> children = h.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    formList.remove(position + 1);
                }
                h.setIcon(getResources().getDrawable(R.drawable.expander_ic_minimized));
                h.setSecondaryText(getString(R.string.collapsed_group));
                h.setColor(Color.WHITE);
                break;
            case COLLAPSED:
                Log.e("carl", "is collapsed group, expanding");
                h.setType(EXPANDED);
                ArrayList<HierarchyElement> children1 = h.getChildren();
                for (int i = 0; i < children1.size(); i++) {
                    Log.e("Carl", "adding child: " + children1.get(i).getFormIndex());
                    formList.add(position + 1 + i, children1.get(i));

                }
                h.setIcon(getResources().getDrawable(R.drawable.expander_ic_maximized));
                h.setSecondaryText(getString(R.string.expanded_group));
                h.setColor(Color.WHITE);

                break;
            case QUESTION:
                // Toast.makeText(this, "Question", Toast.LENGTH_SHORT).show();
                FormEntryActivity.mFormHandler.setFormIndex(h.getFormIndex());
                finish();
                return;
            case CHILD:
                // Toast.makeText(this, "CHILD", Toast.LENGTH_SHORT).show();
                mCurrentIndex = h.getFormIndex();
                mCurrentIndex = nextRelevantIndex(mCurrentIndex);
                Log.e("carl", "clicked index was " + mCurrentIndex);
                refreshView();
                return;
        }

        // Should only get here if we've expanded or collapsed a group
        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);
        this.getListView().setSelection(position);
    }


    private FormIndex nextRelevantIndex(FormIndex index) {
        do {
            index = mForm.incrementIndex(index);
        } while (index.isInForm() && !isRelevant(index));
        return index;
    }


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

        if (relevant) {
            /*
             * if instance flag/condition says relevant, we still have check the
             * <group>/<repeat> hierarchy
             */
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


    @SuppressWarnings("unchecked")
    public Vector<IFormElement> getIndexVector(FormIndex index) {
        return mForm.explodeIndex(index);
    }


    public boolean isEnd(FormIndex mCurrentIndex) {
        if (mCurrentIndex.isEndOfFormIndex())
            return true;
        else
            return false;
    }

    // private boolean indexIsGroup(FormIndex index) {
    // Vector<IFormElement> defs = getIndexVector(index);
    // IFormElement last = (defs.size() == 0 ? null : (IFormElement)
    // defs.lastElement());
    // if (last instanceof GroupDef) {
    // return true;
    // } else {
    // return false;
    // }
    // }
    //
    //
    // private TreeElement resolveReferenceForCurrentIndex(FormIndex i) {
    // return
    // mForm.getDataModel().resolveReference(mForm.getChildInstanceRef(i));
    // }

}
