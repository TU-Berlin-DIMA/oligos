package de.tu_berlin.dima.oligos.type.util;

import java.math.BigDecimal;

public class DecimalOperator implements Operator<BigDecimal> {

  private static boolean isInstantiated = false;
  private static DecimalOperator instance = null;
  
  private DecimalOperator() {}
  
  public static DecimalOperator getInstance() {
    if (isInstantiated) {
      return instance;
    } else {
      isInstantiated = true;
      instance = new DecimalOperator();
      return instance;
    }
  }

  @Override
  public int compare(BigDecimal val1, BigDecimal val2) {
    return val1.compareTo(val2);
  }

  @Override
  public BigDecimal increment(BigDecimal value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BigDecimal increment(BigDecimal value, BigDecimal step) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BigDecimal decrement(BigDecimal value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BigDecimal decrement(BigDecimal value, BigDecimal step) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long difference(BigDecimal val1, BigDecimal val2) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public BigDecimal min(BigDecimal val1, BigDecimal val2) {
    // TODO Auto-generated method stub
    return null;
  }
}
