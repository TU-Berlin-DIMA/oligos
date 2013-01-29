package de.tu_berlin.dima.oligos.type.util.operator.numerical;

import de.tu_berlin.dima.oligos.type.util.operator.AbstractOperator;
import de.tu_berlin.dima.oligos.type.util.operator.Operators;

public abstract class AbstractIntegralOperator<T extends Number & Comparable<T>> extends AbstractOperator<T> {

  @Override
  public T increment(T value) {
    return Operators.increment(value);
  }

  @Override
  public T decrement(T value) {
    return Operators.decrement(value);
  }

}
