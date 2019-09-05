package org.odk.collect.android;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.eval.Indexer;
import org.javarosa.xpath.eval.IndexerType;
import org.javarosa.xpath.eval.PredicateStep;

interface IndexerCreator {
    Indexer getIndexInstance(IndexerType indexType, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps);
}
