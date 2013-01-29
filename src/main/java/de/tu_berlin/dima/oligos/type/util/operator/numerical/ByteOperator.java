package de.tu_berlin.dima.oligos.type.util.operator.numerical;

public class ByteOperator extends AbstractIntegralOperator<Byte> {

  @Override
  public long range(Byte val1, Byte val2) {
    return Math.abs(val2 - val1) + 1;
  }

}
