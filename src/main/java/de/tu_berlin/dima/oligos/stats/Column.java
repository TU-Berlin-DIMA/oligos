package de.tu_berlin.dima.oligos.stats;

import java.util.Set;

import de.tu_berlin.dima.oligos.type.util.Constraint;

public class Column<T> {

  private final String name;
  private final Set<Constraint> constraints;
  private final T min;
  private final T max;
  private final long cardinality;
  private final long numNulls;
  private final Histogram<T> distribution;
  

  public Column(final String name, Set<Constraint> constraints, T min, T max,
      long cardinality, long numNulls, Histogram<T> distribution) {
    this.name = name;
    this.constraints = constraints;
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.numNulls = numNulls;
    this.distribution = distribution;
  }

  public String getName() {
    return name;
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
    if (cardinality == distribution.getCardinality()) {
      return true;
    } else {
      return false;
    }
  }
  
  public boolean isUnique() {
    if (constraints.contains(Constraint.PRIMARY_KEY) || constraints.contains(Constraint.UNIQUE)) {
      return true;
    } else {
      return false;
    }
  }
}
