package org.odk.collect.android;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.eval.Indexer;
import org.javarosa.xpath.eval.IndexerType;
import org.javarosa.xpath.eval.MemoryIndexerImpl;
import org.javarosa.xpath.eval.PredicateStep;
import org.odk.collect.android.provider.DatabaseXPathIndexerImpl;

public class IndexerCreatorImpl implements IndexerCreator {

    private static String indexerMode = "database";

    @Override
    public Indexer getIndexInstance(IndexerType indexType, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps){
        if (indexerMode.equals("memory")) {
            return new MemoryIndexerImpl(indexType, expressionRef, resultRef, predicateSteps);
        } else if (indexerMode.equals("database"))  {
            return  new DatabaseXPathIndexerImpl(indexType, expressionRef, resultRef, predicateSteps);
        }else{
            throw new RuntimeException("Optimization mode not Known");
        }
    }
}
