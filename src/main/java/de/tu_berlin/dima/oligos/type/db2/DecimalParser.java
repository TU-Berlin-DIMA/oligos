package de.tu_berlin.dima.oligos.type.db2;

import java.math.BigDecimal;

import de.tu_berlin.dima.oligos.type.Parser;

public class DecimalParser implements Parser<BigDecimal> {

  @Override
  public BigDecimal parse(String input) {
    return new BigDecimal(input);
  }

}
