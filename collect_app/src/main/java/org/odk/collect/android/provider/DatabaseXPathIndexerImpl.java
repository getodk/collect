package org.odk.collect.android.provider;

import android.content.ContentValues;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.eval.Indexer;
import org.javarosa.xpath.eval.IndexerType;
import org.javarosa.xpath.eval.PredicateStep;
import org.odk.collect.android.dao.XPathExprDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseXPathIndexerImpl implements Indexer {

    private XPathExprDao xPathExprDao;

    public TreeReference expressionRef; //The genericised expression to be indexed - used as the key
    public String expressionString;
    public TreeReference resultRef;  //The genericised pattern of the result to be cached
    public String resultString;
    public PredicateStep[] predicateSteps; //The predicates applied to the expression
    public IndexerType indexerType; // Used to determine how expression would be indexed
    public Map<TreeReference, List<TreeReference>> nodesetExprDict; // Map  used if result is a list of collated nodeset refs
    public Map<TreeReference, IAnswerData> rawValueExprDict; // Used if indexed refs are single Answers

    public DatabaseXPathIndexerImpl(IndexerType indexType, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps) {
        this.xPathExprDao = new XPathExprDao();
        this.expressionRef = expressionRef.removePredicates().genericize();
        this.expressionString = expressionRef.toString();
        this.resultRef = resultRef.removePredicates().genericize();
        this.resultString = resultRef.toString();
        this.indexerType = indexType;
        this.predicateSteps = predicateSteps == null ? new PredicateStep[0] : predicateSteps;
        nodesetExprDict = new HashMap<>();
        rawValueExprDict = new HashMap<>();
    }

    @Override
    public void addToIndex(TreeReference currentTreeReference, TreeElement currentTreeElement) {
        ContentValues values = new ContentValues();
        values.put(XPathProviderAPI.XPathsColumns.PRE_EVAL_EXPR, currentTreeReference.toString());
        values.put(XPathProviderAPI.XPathsColumns.TREE_REF, currentTreeElement.toString());
        xPathExprDao.saveTreeReferenceString(values);
    }

    @Override
    public List<TreeReference> getFromIndex(TreeReference treeReference) {
        return xPathExprDao.getTreeReferenceMatches(treeReference.toString());
    }

    @Override
    public IAnswerData getRawValueFromIndex(TreeReference treeReference) {
        return null;
    }

    @Override
    public boolean belong(TreeReference currentTreeReference) {
        return false;
    }

    @Override
    public void clearCaches() {

    }

    @Override
    public IndexerType getIndexerType() {
        return  indexerType;
    }

    @Override
    public PredicateStep[] getPredicateSteps() {
        return predicateSteps;
    }
}
