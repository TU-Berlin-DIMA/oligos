package de.tu_berlin.dima.oligos.stat.distribution;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public class UniqueDistribution<T> implements Distribution<T> {
  
  private final T min;
  private final T max;
  private final long cardinality;
  private final double probability;
  
  public UniqueDistribution(final T min, final T max, final long cardinality) {
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.probability = 1.0 / cardinality;
  }
  
  public UniqueDistribution(final T min, final T max, final Operator<T> operator) {
    this.min = min;
    this.max = max;
    this.cardinality = operator.range(min, max);
    this.probability = 1.0 / this.cardinality;
  }

  @Override
  public T getMin() {
    return min;
  }

  @Override
  public T getMax() {
    return max;
  }

  @Override
  public long getCardinality() {
    return cardinality;
  }

  @Override
  public double getProbabilityOf(T value) {
    return probability;
  } 
  
}
