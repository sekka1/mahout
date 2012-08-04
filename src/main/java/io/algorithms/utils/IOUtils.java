/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.apache.mahout.classifier.sgd.RecordFactory;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ContinuousValueEncoder;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

import com.google.common.base.Preconditions;

/**
 * Utilities for reading and persisting stuff. 
 */
public final class IOUtils {
    /**
     * Creates a RecordFactory instance which can interpret CSV files.
     * @param columnNameToTypeMap Map whose keys are the column names (as defined in the header), and values are the type.
     * Currently accepted types are "numeric", "word", and "text"
     * @param targetColumnName name of the column that contains the target variable (the class)
     * @param targetCategories list of strings that the target variable can have
     * @return record factory instance.
     */
    public static RecordFactory createRecordFactoryForInputFormat(final Map<String, String> columnNameToTypeMap,
            final String targetColumnName, final List<String> targetClasses) {
        RecordFactory factory = new RajivsSuperCSVRecordFactory(targetColumnName, columnNameToTypeMap);
        factory.defineTargetCategories(targetClasses);
        factory.includeBiasTerm(true);
        return factory;
    }
    
    private static final class RajivsSuperCSVRecordFactory implements RecordFactory {
        final String targetColumnName;
        final Map<String, String> columnNameToTypeMap;
        List<String> targetCategories;
        int maxTargetValue;
        String[] columnNames;
        
        /**
         * @param targetColumnName
         * @param columnNameToTypeMap
         */
        public RajivsSuperCSVRecordFactory(String targetColumnName,
                Map<String, String> columnNameToTypeMap) {
            this.targetColumnName = targetColumnName;
            this.columnNameToTypeMap = columnNameToTypeMap;
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#defineTargetCategories(java.util.List)
         */
        @Override
        public void defineTargetCategories(List<String> list) {
            this.targetCategories = list;
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#maxTargetValue(int)
         */
        @Override
        public RecordFactory maxTargetValue(int i) {
            this.maxTargetValue = i;
            return this;
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#usesFirstLineAsSchema()
         */
        @Override
        public boolean usesFirstLineAsSchema() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#processLine(java.lang.String, org.apache.mahout.math.Vector)
         */
        @Override
        public int processLine(String s, Vector vector) {
            Preconditions.checkNotNull(s);
            Preconditions.checkNotNull(vector);
            Preconditions.checkArgument(vector.size() == columnNames.length);
            String[] values = s.split(",");
            Preconditions.checkArgument(values.length == columnNames.length);
 
            vector.set(0, 1);
            int output = -1;

            // TODO: Fix. Target may not be the last column
            for (int index = 0; index < values.length; index++) {
                String value = values[index];
                String columnName = columnNames[index];
                String type = columnNameToTypeMap.get(columnName);
                double valueDouble = type == "word" ? value.hashCode() : Double.parseDouble(value);
                boolean target = columnName.equals(targetColumnName);
                if (!target) {
                    vector.set(index + 1, valueDouble);
                } else if (targetCategories.contains(value)){
                    return targetCategories.indexOf(value);
                }
            }
            return output;
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#getPredictors()
         */
        @Override
        public Iterable<String> getPredictors() {
            return Arrays.asList(columnNames);
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#getTraceDictionary()
         */
        @Override
        public Map<String, Set<Integer>> getTraceDictionary() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#includeBiasTerm(boolean)
         */
        @Override
        public RecordFactory includeBiasTerm(boolean flag) {
            return this;
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#getTargetCategories()
         */
        @Override
        public List<String> getTargetCategories() {
            return targetCategories;
        }

        /* (non-Javadoc)
         * @see org.apache.mahout.classifier.sgd.RecordFactory#firstLine(java.lang.String)
         */
        @Override
        public void firstLine(String s) {
            columnNames = s.split(",");
        }
    }

}
