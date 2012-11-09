package de.tu_berlin.dima.oligos.profiler;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.Histogram;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.Histograms;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.QuantileHistogram;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class ColumnProfiler<T> implements Profiler<Column<T>> {

  private final ColumnConnector<T> connector;
  private final String schema;
  private final String table;
  private final String column;
  private final String type;
  private final Operator<T> operator;
  private final Parser<T> parser;

  public ColumnProfiler(final String schema, final String table, final String column
      , final String type, final ColumnConnector<T> connector
      , final Operator<T> operator, final Parser<T> parser) {
    this.schema = schema;
    this.table = table;
    this.column = column;
    this.type = type;
    this.connector = connector;
    this.operator = operator;
    this.parser = parser;
  }
  
  public QuantileHistogram<T> getQuantileHistogram() {
    try {
      T min = connector.getMin();
      QuantileHistogram<T> histogram = new QuantileHistogram<T>(min, operator);
      Map<T, Long> qHist = connector.getHistogram();
      long oldCount = 0l;
      for (Entry<T, Long> entry : qHist.entrySet()) {
        T value = entry.getKey();
        long count = entry.getValue();
        if (value.equals(min)) {
          histogram.setMin(operator.decrement(min));
        }
        histogram.addBound(value, count - oldCount);
        oldCount = count;
      }
      return histogram;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Column<T> profile() {
    try {
      Set<Constraint> constraints = connector.getConstraints();
      T min = connector.getMin();
      T max = connector.getMax();
      long cardinality = connector.getCardinality();
      long numNulls = connector.getNumNulls();
      Map<T, Long> rawHistogram = connector.getHistogram();
      QuantileHistogram<T> quantileHistogram = new QuantileHistogram<T>(min, operator);
      for (Entry<T, Long> e : rawHistogram.entrySet()) {
        T upperBound = e.getKey();
        long frequency = e.getValue();
        quantileHistogram.addBound(upperBound, frequency);
      }
      Map<T, Long> mostFrequentValues = connector.getMostFrequentValues();
      Histogram<T> distribution = Histograms.combineHistograms(
          quantileHistogram, mostFrequentValues, operator);
      return new Column<T>(schema, table, column, type, constraints, min, max,
          cardinality, numNulls, distribution, parser);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
