package de.tu_berlin.dima.oligos;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.DB2Connector;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.histogram.Histogram;
import de.tu_berlin.dima.oligos.stat.histogram.Histograms;
import de.tu_berlin.dima.oligos.stat.histogram.QuantileHistogram;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class ColumnProfiler<T> {

  private final DB2Connector connector;
  private final String table;
  private final String column;
  private final String type;
  private final Operator<T> operator;
  private final Parser<T> parser;
  
  public ColumnProfiler(DB2Connector connector, Parser<T> parser, Operator<T> operator, String table, String column, String type) {
    this.connector = connector;
    this.table = table;
    this.column = column;
    this.type = type;
    this.operator = operator;
    this.parser = parser;
  }
  
  public T getMin() {
    String value = "";
    try {
      value = connector.getMin(table, column);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return parser.fromString(value);
  }
  
  public T getMax() {
    String value = "";
    try {
      value = connector.getMax(table, column);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return parser.fromString(value);
  }
  
  public Set<Constraint> getConstraints() {
    Set<Constraint> constraints = Sets.newHashSet();
    try {
      constraints = connector.getColumnConstraints(table, column);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return constraints;
  }
  
  public long getCardinality() {
    try {
      return connector.getCardinality(table, column);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 0l;
  }
  
  public long getNumNulls() {
    try {
      return connector.getNumNulls(table, column);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 0l;
  }
  
  public Map<T, Long> getMostFrequentValues() {
    Map<T, Long> mostFrequent = Maps.newLinkedHashMap();
    try {
      Map<String, Long> stringMostFrequent = connector.getMostFrequentValues(table, column);
      for (Entry<String, Long> entry : stringMostFrequent.entrySet()) {
        T key = parser.fromString(entry.getKey());
        mostFrequent.put(key, entry.getValue());
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return mostFrequent;
  }
  
  public QuantileHistogram<T> getQuantileHistogram() {
    T min = getMin();
    QuantileHistogram<T> histogram = new QuantileHistogram<T>(min, operator);
    try {
      Map<String, Long> qHist = connector.getQuantileHistgram(table, column);
      long oldCount = 0l;
      for (Entry<String, Long> entry : qHist.entrySet()) {
        String key = entry.getKey();
        long count = entry.getValue();
        T value = parser.fromString(key);
        if (value.equals(min)) {
          histogram.setMin(operator.decrement(min));
        }
        histogram.addBound(value, count - oldCount);
        oldCount = count;
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return histogram;
  }

  public Column<T> profile() {
    //boolean hasStats = false;
    boolean isEnum = false;
    try {
      //hasStats = connector.hasStatistics(table, column);
      isEnum = connector.isEnumerated(table, column);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Map<T, Long> mostFrequent = getMostFrequentValues();
    Set<Constraint> constraints = getConstraints();
    long card = getCardinality();
    long numNulls = getNumNulls();
    if (isEnum) {
      T min = getMin();
      T max = getMax();
      return new Column<T>(table, column, type, constraints, min, max, card, numNulls, mostFrequent);
    } else {
      QuantileHistogram<T> qHist = getQuantileHistogram();
      Histogram<T> combHist = Histograms.combineHistograms(qHist, mostFrequent, operator);
      T min = combHist.getMin();
      T max = combHist.getMax();
      return new Column<T>(table, column, type, constraints, min, max, card, numNulls, combHist);
    }
  }
  
  public void printMostFrequentValues() {
    Map<T, Long> mostFrequent = getMostFrequentValues();
    System.out.println("Most Frequent Values");
    for (Entry<T, Long> e : mostFrequent.entrySet()) {
      System.out.println(parser.toString(e.getKey()) + "\t" + e.getValue());
    }
  }
  
  public void printQuantileHistogram() {
    QuantileHistogram<T> qHist = getQuantileHistogram();
    int len = qHist.getNumberOfBuckets();
    System.out.println("Quantile Histogram");
    for (int i = 0; i < len; i++) {
      String lBound = parser.toString(qHist.getLowerBoundAt(i));
      String uBound = parser.toString(qHist.getUpperBoundAt(i));
      long frequency = qHist.getFrequencyAt(i);
      System.out.println(lBound + "\t" + uBound + "\t" + frequency);
    }
  }
}
