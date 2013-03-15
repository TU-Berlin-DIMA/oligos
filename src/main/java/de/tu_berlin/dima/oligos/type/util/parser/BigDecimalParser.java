package de.tu_berlin.dima.oligos.type.util.parser;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalParser extends AbstractParser<BigDecimal> {
  
  private final static int DEFAULT_SCALE = 2;

  @Override
  public BigDecimal fromString(String value) {
    return new BigDecimal(removeQuotes(value));
  }

}
