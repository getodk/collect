package org.odk.collect.android.provider;

import android.content.ContentProviderOperation;
import android.content.ContentValues;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.eval.Indexer;
import org.javarosa.xpath.eval.IndexerType;
import org.javarosa.xpath.eval.PredicateStep;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.XPathExprDao;
import org.odk.collect.android.database.helpers.XPathDatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DatabaseXPathIndexerImpl implements Indexer {

    private XPathExprDao xPathExprDao;

    private XPathPathExpr xPathPathExpr;
    private TreeReference expressionRef; //The genericised expression to be refIsIndexed - used as the key
    private PredicateStep[] predicateSteps; //The predicates applied to the expression
    private IndexerType indexerType; // Used to determine how expression would be refIsIndexed
    private List<String> dbColumns;
    private boolean loaded;
    private String expressionString;
    private String resultRefString;
    private Map<String, ContentValues> insertValuesMap = new HashMap();

    public DatabaseXPathIndexerImpl(IndexerType indexType,XPathPathExpr xPathPathExpr, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps) {

        this.xPathExprDao = XPathExprDao.getInstance();

        this.xPathPathExpr = xPathPathExpr;
        this.expressionRef = expressionRef.removePredicates().genericize();
        this.expressionString = expressionRef.toString();
        this.indexerType = indexType;
        this.resultRefString = resultRef.toString();
        this.predicateSteps = predicateSteps == null ? new PredicateStep[0] : predicateSteps;
        dbColumns = new ArrayList();
    }

    @Override
    public void prepIndex() {
        getColumnsNames();
        //This is not the best way to check if all leafs that correspond to this expression have been indexed
        //Better way is to keep that info in a seperate table
        loaded = xPathExprDao.fetchIndexCount(expressionRef) > 0;
    }

    @Override
    public void addToIndex(TreeReference currentTreeReference, TreeElement currentTreeElement) {
        if(!loaded){
            String fieldName = currentTreeElement.getName();
            TreeReference  currentTreeReferenceClone = currentTreeReference.clone();
            currentTreeReferenceClone.removeLastLevel();//Trim to level
            String refString = currentTreeReferenceClone.toString(true);
            ContentValues values = insertValuesMap.get(refString);
            if(values != null ){
                addColumnIfNotExists(fieldName);
                values.put(fieldName, currentTreeElement.getValue().getDisplayText());
            }else{
                addColumnIfNotExists(fieldName);
                values = new ContentValues();
                values.put(fieldName, currentTreeElement.getValue().getDisplayText());
                values.put(XPathProviderAPI.XPathsColumns.EVAL_EXPR, expressionRef.toString());
                values.put(XPathProviderAPI.XPathsColumns.TREE_REF, currentTreeReferenceClone.toString(true));
                insertValuesMap.put(currentTreeReferenceClone.toString(true), values);
            }
        }
    }

    private void addColumnIfNotExists(String columnName){
        boolean isExists = false;
        List<String> columnNames = getColumnsNames();
        if (columnNames != null) {
            Iterator<String> iterator = columnNames.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                if (columnName.equalsIgnoreCase(name)) {
                    isExists = true;
                    break;
                }
            }
        }

        if(!isExists){
            if(xPathExprDao.createColumn(columnName)){
                dbColumns.add(columnName);
            }else{
                throw new RuntimeException("Unable to create index column for  node : " + columnName);
            }
        }
    }

    private List<String> getColumnsNames(){
        if(dbColumns.size() < 1){
           dbColumns =  xPathExprDao.getColumnsFromDB();
        }
        return dbColumns;
    }

    @Override
    public void deleteIndex(){
        File databseFile = new File(Collect.METADATA_PATH + File.separator + XPathDatabaseHelper.DATABASE_NAME);
        if(databseFile.exists()){
            databseFile.delete();
        }
        loaded = false;
    }

    @Override
    public List<TreeReference> resolveFromIndex(TreeReference treeReference) {
        return xPathExprDao.getTreeReferenceMatches(treeReference.toString());
    }

    @Override
    public IAnswerData getRawValueFromIndex(TreeReference treeReference) {
        return xPathExprDao.queryScalerEqXPathExpression(treeReference);
    }

    @Override
    public boolean belong(TreeReference currentTreeReference) {
        String instanceName = currentTreeReference.getInstanceName();
        if(instanceName != null && !instanceName.equals(expressionRef.getInstanceName())){
            return  false;
        }
        String treeRefString = currentTreeReference.toString(false);
        if (indexerType.equals(IndexerType.GENERIC_PATH) ||
                indexerType.equals(IndexerType
                        .LAST_EQUAL_PREDICATE_PATH)
        ) {
            return treeRefString.equals(expressionString) ||
                    treeRefString.equals(resultRefString) ;
        }else if (indexerType.equals(IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH)) {
            return treeRefString.equals(expressionString) ||
                    treeRefString.equals(resultRefString) ;
        }
        return false;
    }

    @Override
    public XPathPathExpr getExpression() {
        return xPathPathExpr;
    }

    @Override
    public void finalizeIndex() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for(Map.Entry<String, ContentValues> valuesEntry: insertValuesMap.entrySet()){
            operations.add(
                    ContentProviderOperation
                            .newInsert(XPathProviderAPI.XPathsColumns.CONTENT_URI)
                            .withValues(valuesEntry.getValue())
                            .build()
            );
        }
        try {
            if(!operations.isEmpty())
           xPathExprDao.flushIndexes(operations);
            if(!insertValuesMap.isEmpty())
           insertValuesMap.clear();
        } catch (Exception e) {
            e.printStackTrace();

        }
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
