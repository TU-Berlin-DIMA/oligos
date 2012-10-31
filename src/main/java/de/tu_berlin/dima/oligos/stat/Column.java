package de.tu_berlin.dima.oligos.stat;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.stat.histogram.CustomHistogram;
import de.tu_berlin.dima.oligos.stat.histogram.Histogram;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.operator.OperatorManager;

public class Column<T> {

  private final String table;
  private final String column;
  private final String type;
  private final Set<Constraint> constraints;
  private final T min;
  private final T max;
  private final long cardinality;
  private final long numNulls;
  private final Histogram<T> distribution;
  private final Map<T, Long> mostFrequent;
  

  public Column(final String table, final String column, final String type, Set<Constraint> constraints, T min, T max,
      long cardinality, long numNulls, Histogram<T> distribution) {
    this.table = table;
    this.column = column;
    this.type = type;
    this.constraints = constraints;
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.numNulls = numNulls;
    this.distribution = distribution;
    this.mostFrequent = Maps.newHashMap();
  }
  
  @SuppressWarnings("unchecked")
  public Column(final String table, final String column, final String type, Set<Constraint> constraints, T min, T max,
      long cardinality, long numNulls, Map<T, Long> mostFrequent) {
    this.table = table;
    this.column = column;
    this.type = type;
    this.constraints = constraints;
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.numNulls = numNulls;
    this.distribution = new CustomHistogram<T>((Operator<T>) OperatorManager.getInstance().getOperator("", table, column));
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
  
  public String getType() {
    return type;
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
    return !mostFrequent.isEmpty() && distribution.isEmpty();
  }
  
  public boolean isUnique() {
    if (constraints.contains(Constraint.PRIMARY_KEY) || constraints.contains(Constraint.UNIQUE)) {
      return true;
    } else {
      return false;
    }
  }
  
  public boolean hasDistribution() {
    return !distribution.isEmpty();
  }
  
  public long getNumberOfValues() {
    if (hasDistribution()) {
      return distribution.getTotalNumberOfValues();
    } else {
      long total = 0;
      for (long c : mostFrequent.values()) {
        total += c;
      }
      return total;
    }
  }
  
  public Histogram<T> getDistribution() {
    return distribution;
  }
  
  public Map<T, Long> getDomain() {
    return mostFrequent;
  }
}
