package de.tu_berlin.dima.oligos.type.db2;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.tu_berlin.dima.oligos.type.Parser;

public class DecimalParser implements Parser<BigDecimal> {
  
  private final int scale;
  
  public DecimalParser(final int scale) {
    this.scale = scale;
  }

  @Override
  public BigDecimal parse(String input) {
    return new BigDecimal(input).setScale(scale, RoundingMode.DOWN);
  }
  
  @Override
  public String format(BigDecimal input) {
    return input.setScale(scale, RoundingMode.DOWN).toString();
  }

}
