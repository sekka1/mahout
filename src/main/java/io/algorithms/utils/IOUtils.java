/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.apache.mahout.classifier.sgd.RecordFactory;
import org.apache.mahout.math.Vector;

import com.google.common.base.Preconditions;
import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import com.googlecode.jcsv.reader.internal.DefaultCSVEntryParser;

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
    public static CsvRecordFactory createRecordFactoryForInputFormat(final Map<String, String> columnNameToTypeMap,
            final String targetColumnName, final List<String> targetClasses) {
        CsvRecordFactory factory = new CsvRecordFactory(targetColumnName, columnNameToTypeMap);
        factory.defineTargetCategories(targetClasses);
        factory.includeBiasTerm(true);
        return factory;
    }
    
    private static final class RajivsSuperCSVRecordFactory implements RecordFactory {
        final ConcurrentMap<String, Integer> dict = new ConcurrentHashMap<String, Integer>();
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
            System.out.println(s);
            Preconditions.checkNotNull(vector);
            Preconditions.checkArgument(vector.size() == columnNames.length);
            s = s.replace("\\\"", "");
            CSVReader<String[]> reader = new CSVReaderBuilder<String[]>(new StringReader(s)).strategy(CSVStrategy.UK_DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
 
            int output = -1;

            try {
                String[] values = reader.readNext();
                Preconditions.checkArgument(values.length == columnNames.length);
                // TODO: Fix. Target may not be the last column
                vector.set(values.length - 1, 1);
                for (int index = 0; index < values.length; index++) {
                    String value = values[index];
                    String columnName = columnNames[index];
                    String type = columnNameToTypeMap.get(columnName);
                    double valueDouble = 0;
                    if (type.equals("word")) {
                        synchronized (dict) {
                            if (dict.containsKey(value)) {
                                valueDouble = dict.get(value);
                            } else {
                                dict.put(value, dict.size());
                                valueDouble = dict.size();
                            }
                        }
                    } else if (type.equals("numeric")) {
                        valueDouble = value.isEmpty() ? 0 : Double.parseDouble(value.replaceAll(",", ""));
                    }
                    boolean target = columnName.equals(targetColumnName);
                    if (!target) {
                        vector.set(index, valueDouble);
                    } else if (targetCategories.contains(value)){
                        return targetCategories.indexOf(value);
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
            CSVReader<String[]> reader = new CSVReaderBuilder<String[]>(new StringReader(s)).strategy(CSVStrategy.UK_DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
            try {
                columnNames = reader.readNext();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
