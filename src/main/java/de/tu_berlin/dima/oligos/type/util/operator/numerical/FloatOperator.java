package de.tu_berlin.dima.oligos.type.util.operator.numerical;

import de.tu_berlin.dima.oligos.type.util.operator.AbstractOperator;

public class FloatOperator extends AbstractOperator<Float> {

  @Override
  public Float increment(Float value) {
    return Math.nextUp(value);
  }

  @Override
  public Float decrement(Float value) {
    return Math.nextAfter(value, Float.NEGATIVE_INFINITY);
  }

  @Override
  public long range(Float val1, Float val2) {
    int a = Float.floatToIntBits(val1);
    int b = Float.floatToIntBits(val2);
    return a - b + 1L;
  }
}
