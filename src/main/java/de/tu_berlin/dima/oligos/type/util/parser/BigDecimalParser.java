package de.tu_berlin.dima.oligos.type.util.parser;

import java.math.BigDecimal;

public class BigDecimalParser extends AbstractParser<BigDecimal> {
  
  @Override
  public BigDecimal fromString(String value) {
    return new BigDecimal(removeQuotes(value));
  }

}
