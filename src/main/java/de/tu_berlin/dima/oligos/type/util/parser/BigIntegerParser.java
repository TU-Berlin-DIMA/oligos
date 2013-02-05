package de.tu_berlin.dima.oligos.type.util.parser;

import java.math.BigInteger;

public class BigIntegerParser implements Parser<BigInteger> {

  @Override
  public BigInteger fromString(String value) {
    return new BigInteger(value);
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
