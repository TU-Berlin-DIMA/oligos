package de.tu_berlin.dima.oligos.type.util.parser;

import java.math.BigInteger;

public class BigIntegerParser extends AbstractParser<BigInteger> {

  @Override
  public BigInteger fromString(String value) {
    return new BigInteger(removeQuotes(value));
  }

}
