package org.odk.collect.android.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.logic.FormHandler;
import org.odk.collect.android.logic.HierarchyElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * 
 * TODO:  WARNING, this file is by no means complete, or correct for that matter.  
 * It is hacky attempt #1 to make a hierarchy viewer by stealing a bunch of things from
 * formHandler.  JavaRosa should give us better methods to accomplish this
 * (and will in the future...fingers crossed)
 * But until then, we're trying...
 *
 */
public class FormHierarchyActivity extends ListActivity {

    FormDef mForm;
    int state;

    private static final int INDEX = 2;
    private static final int GROUP = 3;

    private FormIndex mCurrentIndex;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hierarchy_layout);

        
        // We'll use formhandler to set the CurrentIndex before returning to FormEntry
        FormHandler mFormHandler = FormEntry.mFormHandler;
        

        mForm = mFormHandler.getForm();

        mCurrentIndex = mFormHandler.getIndex();
        if (mCurrentIndex.isBeginningOfFormIndex()) {
            // we always have to start on a valid formIndex
            mCurrentIndex = mForm.incrementIndex(mCurrentIndex);
        }



        Button b = (Button) findViewById(R.id.backbutton);
        b.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                if (mCurrentIndex == null) {
                    mCurrentIndex = FormIndex.createBeginningOfFormIndex();
                    mCurrentIndex = mForm.incrementIndex(mCurrentIndex);
                }

                // when we go 'back', we switch the state we're in.
                switch (state) {
                    case GROUP:
                        refreshForIndex();
                        break;
                    case INDEX:
                        refreshForGroup();
                        break;
                }
            }

        });
        Log.e("Carl", "refreshing for index");

        refreshForIndex();

    }


    // used to go 'back', the only problem is this changes whatever it's referencing
    FormIndex stepIndexOut(FormIndex index) {
        if (index.isTerminal()) {
            return null;
        } else {
            index.setNextLevel(stepIndexOut(index.getNextLevel()));
            return index;
        }
    }


  

    private void refreshForGroup() {
        state = GROUP;
        Log.e("FormHierarchyViewer", "refreshing for GROUP ***********************************");
        List<HierarchyElement> formList = new ArrayList<HierarchyElement>();
        
        FormIndex asdf = mCurrentIndex;
        String repeatedGroup = mForm.getChildInstanceRef(asdf).toString(false);
        IFormElement el = mForm.getChild(asdf);
        GroupDef gr = (GroupDef) el;
        String groupName = gr.getLongText();
        int count = 1;

        int level = 0;
        FormIndex ref = asdf;
        while (ref.getNextLevel() != null) {
            ref = ref.getNextLevel();
            level++;
        }

        int local = ref.getLocalIndex();
       
        // go back to start of groups
        while (!(local == ref.getLocalIndex() && ref.getInstanceIndex() == 0 && ref.isTerminal())) {
            asdf = prevIndex(asdf);
            ref = asdf;
            for (int i = 0; i < level; i++) {
                ref = ref.getNextLevel();
            }
        }
       


        while (!isEnd(asdf)) {
            String s = mForm.getChildInstanceRef(asdf).toString(false);
            String h = "\t";
            h += s + "\t" + asdf.toString();
            IFormElement e = mForm.getChild(asdf);


            if (!(s.startsWith(repeatedGroup))) {
                // we've left this particular group
                break;
            }


            if (s.equalsIgnoreCase(repeatedGroup)) {
                // we put the first real element of the group as the index
                // and also check to make sure we're still in this group because
                // the last
                // formindex references an empty group
                asdf = nextRelevantIndex(asdf);
                if (asdf.isEndOfFormIndex()) break;
                String path = mForm.getChildInstanceRef(asdf).toString(false);
                if (path.startsWith(repeatedGroup)) {
                    Log.e("Carl", "adding with local = " + asdf.getLocalIndex()
                            + " and instance = " + asdf.getInstanceIndex() + " and all = "
                            + asdf.toString());
                    formList.add(new HierarchyElement(groupName + " " + count, "Group",
                            getResources().getDrawable(android.R.drawable.arrow_down_float), 1,
                            asdf));
                    count++;
                }
            }

            asdf = nextRelevantIndex(asdf);

        }


        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);
    }


    private void refreshForIndex() {
        state = INDEX;
        Log.e("FormHierarchyViewer", "refreshing for index ***********************************");
        List<HierarchyElement> formList = new ArrayList<HierarchyElement>();

        FormIndex asdf = mCurrentIndex;
        String repeatGroup = "norepeatsinhere";

        FormIndex startTest = stepIndexOut(asdf);
        boolean beginning = (startTest == null);
        String displayGroup = "";

        if (!beginning) {
            // we're displaying only things only within a given group
            displayGroup = mForm.getChildInstanceRef(startTest).toString(false);
            asdf = nextRelevantIndex(asdf);

        } else {
            asdf = FormIndex.createBeginningOfFormIndex();
            asdf = nextRelevantIndex(asdf);
        }


        while (!isEnd(asdf)) {
            String s = mForm.getChildInstanceRef(asdf).toString(false);
            String h = "\t";
            h += s + "\t" + asdf.toString();
            IFormElement e = mForm.getChild(asdf);

            // for root we want to display everything
            if (!beginning && (s.equalsIgnoreCase(displayGroup) || !s.startsWith(displayGroup))) {
                // this is the next repeat or outside our current group
                break;
            }

            if (s.startsWith(repeatGroup)) {
                // skip everything that would be in this group
                asdf = nextRelevantIndex(asdf);
                continue;
            }
          

            if (e instanceof GroupDef) {
                GroupDef g = (GroupDef) e;
                h += "\t" + g.getLongText() + "\t" + g.getRepeat();

                if (g.getRepeat() && !s.startsWith(repeatGroup)) {
                    // we have a new repeated group that we haven't seen before
                    repeatGroup = s;
                    formList.add(new HierarchyElement(g.getLongText(), "Group", getResources()
                            .getDrawable(android.R.drawable.arrow_down_float), 0, asdf));
                    asdf = nextRelevantIndex(asdf);
                    continue;

                }
            } else if (e instanceof QuestionDef) {
                QuestionDef q = (QuestionDef) e;
                h += "\t" + q.getLongText();
                Log.e("FHV", h);
                formList.add(new HierarchyElement(q.getLongText(), "Question", null, 2, asdf));
            } else {
                Log.e("error", "we shouldn't get here");
            }
            

            asdf = nextRelevantIndex(asdf);
        }
       
        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);
    }



    private FormIndex prevIndex(FormIndex index) {
        do {
            index = mForm.decrementIndex(index);
        } while (index.isInForm() && !isRelevant(index));
        return index;
    }



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HierarchyElement t = (HierarchyElement) getListAdapter().getItem(position);


        switch (t.getType()) {
            case 2:
            default:
                Toast.makeText(this, "Question", Toast.LENGTH_SHORT).show();
                break;
            case 0:
                mCurrentIndex = t.getFormIndex();
                refreshForGroup();
                break;
            case 1:
                mCurrentIndex = t.getFormIndex();
                refreshForIndex();
                break;
        }
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
             * if instance flag/condition says relevant, we still have check the <group>/<repeat> hierarchy 
             * 
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


    private boolean indexIsGroup(FormIndex index) {
        Vector<IFormElement> defs = getIndexVector(index);
        IFormElement last = (defs.size() == 0 ? null : (IFormElement) defs.lastElement());
        if (last instanceof GroupDef) {
            return true;
        } else {
            return false;
        }
    }


    private TreeElement resolveReferenceForCurrentIndex(FormIndex i) {
        return mForm.getDataModel().resolveReference(mForm.getChildInstanceRef(i));
    }

}
