package de.tu_berlin.dima.oligos.type.util.operator;

public abstract class AbstractOperator<T extends Comparable<T>>
implements Operator<T> {

  @Override
  public int compare(T o1, T o2) {
    return o1.compareTo(o2);
  }

  @Override
  public T min(T val1, T val2) {
    return (val1.compareTo(val2) <= 0) ? val1 : val2; 
  }

  @Override
  public T max(T val1, T val2) {
    return (val1.compareTo(val2) >= 0) ? val1 : val2;
  }

}
