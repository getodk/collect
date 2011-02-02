package org.odk.collect.android.views.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathStringLiteral;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.views.GroupView;
import org.odk.collect.android.widgets.AbstractQuestionWidget;
import org.odk.collect.android.widgets.IMultipartSelectWidget;
import org.odk.collect.android.widgets.WidgetFactory;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConditionalFieldList  implements IGroupLayout {
	
	// maintain separate list of all question views...
	private List<AbstractQuestionWidget> viewList = new ArrayList<AbstractQuestionWidget>();

    /**
     * Return map of ( TreeReference : String ) that contains the 
     * TreeReference values of all the form widgets that have a
     * simple relevancy condition involving the given select widget.
     * 
     * The String associated with the TreeReference is the item value 
     * of the select widget for which they are relevant.
     * 
     * A simple relevancy condition is a binding of the form, e.g.:
     * 
     * <bind nodeset="/widgets/int_a" relevant="selected(/widgets/select, 'a')" />
     * 
     * I.e., the relevant="..." expression is a selected() predicate whose
     * second argument is a single string literal that corresponds to a 
     * selection value.
     *     
     * If a TreeReference is relevant for two or more values, it will 
     * not appear in the map.
     * 
     * @param fi form index of the selectOne or selectMulti form widget
     * @return map of (TreeReference => select-value)
     */
    @SuppressWarnings("unchecked")
	private Map<TreeReference,String> findSubfieldPairings(FormIndex fi) {
    	// we want a map of ( select-value => { list of tree references relevant for select-value } )
    	// such that the tree references can be then correlated to a form index of the subsequent controls.
    	// It would be improper for the same tree reference to appear with more than one value.
    	// To ensure that we don't double-map anything, we actually build the map backwards
    	// as ( TreeReference => string ) and keep a list of TreeReference that are double-mapped.
    	Map<TreeReference,String> treeRefValueMap = new HashMap<TreeReference,String>();
    	List<TreeReference> badTreeReferences = new ArrayList<TreeReference>();

    	// the vector of computations that depend upon the value of this form index are maintained in the 
    	// triggerIndex hash.  This has explicit dependencies but on implicit (nested) dependencies.  For 
    	// example, an entire group could have a relevant="..." binding; in that case, the elements within 
    	// that group have an implicit relevant condition.  We assume that all associations for the 
    	// conditional-field-list are explicit.
    	FormEntryController fec = Collect.getInstance().getFormEntryController();
    	Object o = fec.getModel().getForm().triggerIndex.get(fi.getReference().genericize());
    	if ( o == null || !(o instanceof Vector) ) return treeRefValueMap; // just to keep everyone honest...
    	Vector<Triggerable> v = (Vector<Triggerable>) o;
    	
    	// now winnow this list down to just the "relevant=" conditions
    	for ( int i = 0 ; i < v.size() ; ++i ) {
    		Triggerable t = v.elementAt(i);
    		if (t instanceof Condition) {
    			Condition c = (Condition) t;
    			if ( (c.trueAction == Condition.ACTION_SHOW) && (c.expr instanceof XPathConditional )) {
    				// this is a relevant= condition.  
    				// find only the top-level expressions that are selected(self,tag)
    				XPathConditional xe = (XPathConditional) c.expr;
    				if ( xe.getExpr() instanceof XPathFuncExpr ) {
    					XPathFuncExpr xfe = (XPathFuncExpr) xe.getExpr();
    					if ( "selected".equals(xfe.id.toString()) ) {
    						// ok. we have a selected(...) as the top level expression.
    						// assume the LHS is the selectOne or selectMulti referenced by the fi, the FormIndex.
    						// get the RHS (the selection value).
    						XPathExpression rhs = xfe.args[1];
    						if ( rhs instanceof XPathStringLiteral ) {
    							String valueTag = ((XPathStringLiteral) rhs).s;
    							// and now collect all the targets for this tag
    							// and ensure that they are not double-mapped.
    							// if any are, add them to the list of refs to purge.
    							Vector targets = c.getTargets();
    							for ( int j = 0 ; j < targets.size() ; ++j ) {
    								if ( targets.elementAt(j) instanceof TreeReference ) {
    									TreeReference tr = (TreeReference) targets.elementAt(j);
    									if ( treeRefValueMap.containsKey(tr) ) {
    										badTreeReferences.add(tr);
    									} else {
    										treeRefValueMap.put(tr, valueTag);
    									}
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	
    	// eliminate bad tree references
    	for ( TreeReference t : badTreeReferences ) {
    		treeRefValueMap.remove(t);
    	}
    	
    	// and the final map now has all singly-mapped 
    	// TreeReference => select-value-tag 
    	// associations.  This may have more than we
    	// need, because other tags elsewhere in the form
    	// may depend upon the value of this select statement
    	// but it will at least contain the fields within 
    	// the conditional field list.
    	return treeRefValueMap;
    }

    public ConditionalFieldList() {
    }

    @Override
	public void buildView(GroupView view, List<FormIndex> indices, String instancePath, FormEntryCaption[] groups) {

        FormEntryModel model = Collect.getInstance().getFormEntryController().getModel();

		LinearLayout mView = new LinearLayout(view.getContext());
        mView.setOrientation(LinearLayout.VERTICAL);
        mView.setGravity(Gravity.TOP);
        mView.setPadding(0, 7, 0, 0);

        LinearLayout.LayoutParams mLayout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        mLayout.setMargins(10, 0, 10, 0);
        
        // put crumb trail on top...
        String crumbTrail = GroupView.getGroupText(groups);
        if (crumbTrail.length() > 0) {
            TextView tv = new TextView(view.getContext());
            tv.setText(crumbTrail.substring(0, crumbTrail.length() - 3));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AbstractQuestionWidget.TEXTSIZE - 7);
            tv.setPadding(0, 0, 0, 5);
            mView.addView(tv, AbstractQuestionWidget.COMMON_LAYOUT);
        }

        FormIndex fiFirst = indices.get(0);
        FormEntryPrompt selectPrompt = model.getQuestionPrompt(fiFirst);
        Vector<SelectChoice> values = selectPrompt.getSelectChoices();

        Map<TreeReference,String> dependencies = findSubfieldPairings(fiFirst);

    	// we will need to rearrange the indices list into 
    	// two parts -- a map of value-tag => {indexA... }
    	// and a list of indices that don't map to a single value-tag.
    	Map<String, List<FormIndex>> selectValueMap = new HashMap<String,List<FormIndex>>();
    	List<FormIndex> residual = new ArrayList<FormIndex>();
    	
    	boolean isFirst = true;
        for ( FormIndex fi : indices) {
        	String value = dependencies.get(fi.getReference().genericize());
        	if ( isFirst || value == null ) {
        		isFirst = false;
        		// doesn't have a unique association with the
        		// first widget.
        		residual.add(fi);
        	} else {
        		List<FormIndex> fiList = selectValueMap.get(value);
        		if ( fiList == null ) {
        			fiList = new ArrayList<FormIndex>();
        			selectValueMap.put(value, fiList);
        		}
        		fiList.add(fi);
        	}
        }
        
        // OK.  So we have the mapping for handling the select
        // widget (the first widget) and we have the residual
        // list of widgets that don't belong under the select.
        // The select widget is the first on the residual list.
        
        int iResidual = 0;
        // lay out the select widget...
        FormIndex fi = residual.get(iResidual);

		FormEntryPrompt formEntryPrompt = Collect.getInstance().getFormEntryController().getModel().getQuestionPrompt(fi);
        // if question or answer type is not supported, use text widget
		AbstractQuestionWidget qvSelect = WidgetFactory.createWidgetFromPrompt(view.getHandler(), formEntryPrompt, view.getContext(), instancePath);
        viewList.add(qvSelect);
		
        if ( values != null && qvSelect instanceof IMultipartSelectWidget ) {
        	IMultipartSelectWidget qvMultiPart = ((IMultipartSelectWidget) qvSelect);

        	qvSelect.buildViewStart();

	        for ( int i = 0 ; i < values.size(); ++i ) {
	        	SelectChoice c = values.get(i);
	        	
	        	ViewGroup vg = qvMultiPart.buildSelectElement(c);

	        	List<FormIndex> nestedWidgets = selectValueMap.get(c.getValue());

	        	if ( nestedWidgets != null ) {
		        	for ( FormIndex nestedFormIndex : nestedWidgets ) {
		        		formEntryPrompt = Collect.getInstance().getFormEntryController().getModel().getQuestionPrompt(nestedFormIndex);
		        		AbstractQuestionWidget nestedWidget =
		        			WidgetFactory.createWidgetFromPrompt(view.getHandler(), formEntryPrompt, view.getContext(), instancePath);

		        		nestedWidget.buildView(view);
		                
		        		vg.addView(nestedWidget, mLayout);
		                viewList.add(nestedWidget);
		        	}
	        	}
	        }
	        qvSelect.buildViewEnd(view);

        } else {
        	Log.e(getClass().getName(), "Partial view construction attempted on non-multipart-build widget or one with no dependencies");
        	qvSelect.buildView(view);
        }

        mView.addView(qvSelect);
        
        // and lay out all the remaining widgets on the form
        for ( ++iResidual ; iResidual < residual.size() ; ++iResidual ) {

        	fi = residual.get(iResidual);

        	// Add a dividing line before each element
            View divider = new View(view.getContext());
            
            divider.setId(AbstractQuestionWidget.newUniqueId());
            divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
            divider.setMinimumHeight(3);
            mView.addView(divider);

    		formEntryPrompt = Collect.getInstance().getFormEntryController().getModel().getQuestionPrompt(fi);
            // if question or answer type is not supported, use text widget
    		AbstractQuestionWidget mQuestionWidget = WidgetFactory.createWidgetFromPrompt(view.getHandler(), formEntryPrompt, view.getContext(), instancePath);

    		mQuestionWidget.buildView(view);
            
            mView.addView((View) mQuestionWidget, mLayout);
            viewList.add(mQuestionWidget);
        }
        
        view.addView(mView);
    }
    
	@Override
	public AbstractQuestionWidget getDefaultFocus() {
		return viewList.get(0);
	}

	@Override
	public AbstractQuestionWidget getQuestionWidget(FormIndex subIndex) {
		for ( AbstractQuestionWidget qv : viewList ) {
			if ( qv.getFormIndex().equals(subIndex) ) return qv;
		}
		return null;
	}

	@Override
	public void unregister() {
		for ( AbstractQuestionWidget v : viewList ) {
			v.unregister();
		}
	}
}
