package de.tu_berlin.dima.oligos.stat;

import java.util.Map;
import java.util.Set;

import de.tu_berlin.dima.oligos.stat.histogram.Histogram;
import de.tu_berlin.dima.oligos.type.util.Constraint;

public class Column<T> {

  private final String table;
  private final String column;
  private final Set<Constraint> constraints;
  private final T min;
  private final T max;
  private final long cardinality;
  private final long numNulls;
  private final Histogram<T> distribution;
  private final Map<T, Long> mostFrequent;
  

  public Column(final String table, final String column, Set<Constraint> constraints, T min, T max,
      long cardinality, long numNulls, Histogram<T> distribution) {
    this.table = table;
    this.column = column;
    this.constraints = constraints;
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.numNulls = numNulls;
    this.distribution = distribution;
    this.mostFrequent = null;
  }
  
  public Column(final String table, final String column, Set<Constraint> constraints, T min, T max,
      long cardinality, long numNulls, Map<T, Long> mostFrequent) {
    this.table = table;
    this.column = column;
    this.constraints = constraints;
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.numNulls = numNulls;
    this.distribution = null;
    this.mostFrequent = mostFrequent;
  }
  
  public String getTable() {
    return table;
  }
  
  public String getColumn() {
    return column;
  }

  public String getName() {
    return table + "." + column;
  }

  public Set<Constraint> getConstraints() {
    return constraints;
  }

  public T getMin() {
    return min;
  }

  public T getMax() {
    return max;
  }

  public long getCardinality() {
    return cardinality;
  }

  public long getNumNulls() {
    return numNulls;
  }
  
  public boolean isEnumerated() {
    return mostFrequent != null && distribution == null;
  }
  
  public boolean isUnique() {
    if (constraints.contains(Constraint.PRIMARY_KEY) || constraints.contains(Constraint.UNIQUE)) {
      return true;
    } else {
      return false;
    }
  }
}
