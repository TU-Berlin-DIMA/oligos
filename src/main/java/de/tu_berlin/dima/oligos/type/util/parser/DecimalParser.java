package de.tu_berlin.dima.oligos.type.util.parser;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalParser implements Parser<BigDecimal> {
  
  private final static int DEFAULT_SCALE = 2;
  
  private static boolean isInstantiated = false;
  private static DecimalParser instance = null;
  
  private DecimalParser() {}
  
  public static DecimalParser getInstance() {
    if (isInstantiated) {
      return instance;
    } else {
      isInstantiated = true;
      instance = new DecimalParser();
      return instance;
    }
  }

  @Override
  public BigDecimal fromString(String value) {
    return (new BigDecimal(value)).setScale(DEFAULT_SCALE, RoundingMode.DOWN);
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
