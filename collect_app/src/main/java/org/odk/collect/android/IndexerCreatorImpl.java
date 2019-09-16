package org.odk.collect.android;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.eval.Indexer;
import org.javarosa.xpath.eval.IndexerCreator;
import org.javarosa.xpath.eval.IndexerType;
import org.javarosa.xpath.eval.MemoryIndexerImpl;
import org.javarosa.xpath.eval.PredicateStep;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.odk.collect.android.provider.DatabaseXPathIndexerImpl;

public class IndexerCreatorImpl implements IndexerCreator {

    public  static int NONE = 0; // Does not create index any expression
    public  static int MEMORY_ONLY = 1; // Uses Memory Index for all expressions
    public  static int DATABASE = 2; // Uses the Database index for all expressions
    public  static int MIXED = 3; // Uses Memory index for the calculate expression , database indexing not completely implemented

    public static int indexerMode = NONE;

    @Override
    public Indexer getIndexer(IndexerType indexType, XPathPathExpr xPathPathExpr, TreeReference expressionRef, TreeReference resultRef) {
        return getIndexer(indexType, xPathPathExpr, expressionRef,resultRef, null);
    }

    @Override
    public Indexer getIndexer(IndexerType indexType, XPathPathExpr xPathPathExpr, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps) {
            if (indexerMode == MEMORY_ONLY) {
                return new MemoryIndexerImpl(indexType, xPathPathExpr, expressionRef, resultRef, predicateSteps);
            } else if (indexerMode == DATABASE)  {
                if(indexType.equals(IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH)){
                    return  new DatabaseXPathIndexerImpl(indexType, xPathPathExpr, expressionRef, resultRef, predicateSteps);
                }else {
                    return null;
                }
            } else if (indexerMode == MIXED)  { //The memory indexer is used in this case
                if(indexType.equals(IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH)){
                    return  new DatabaseXPathIndexerImpl(indexType, xPathPathExpr, expressionRef, resultRef, predicateSteps);
                }else {
                    return new MemoryIndexerImpl(indexType, xPathPathExpr, expressionRef, resultRef, predicateSteps);
                }
            } else{
                throw new RuntimeException("Optimization mode not Known");
            }
    }
}
