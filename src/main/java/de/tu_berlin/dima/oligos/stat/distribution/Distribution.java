package de.tu_berlin.dima.oligos.stat.distribution;

public interface Distribution<T> {
  
  public T getMin();
  
  public T getMax();
  
  public long getCardinality();
  
  public double getProbabilityOf(T value);

}
