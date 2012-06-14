package de.tu_berlin.dima.oligos.type;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalType implements Type<DecimalType> {
  
  private static int scale = 2;
  private static BigDecimal step = new BigDecimal(0.01);
  
  private final BigDecimal value;
  
  public DecimalType(BigDecimal value) {
    this.value = value;
  }
  
  public DecimalType(String value) {
    this.value = (new BigDecimal(value)).setScale(scale, RoundingMode.DOWN);
  }
  
  public static void setScale(int scale) {
    scale = 2;
  }

  @Override
  public int compareTo(DecimalType other) {
    return value.compareTo(other.value);
  }

  @Override
  public DecimalType increment() {
    return new DecimalType(value.add(step));
  }

  @Override
  public DecimalType decrement() {
    return new DecimalType(value.subtract(step));
  }

  @Override
  public int difference(DecimalType other) {
    double v1 = this.value.doubleValue();
    double v2 = other.value.doubleValue();
    return (int) Math.abs(Math.round(v1 - v2));
  }

  @Override
  public DecimalType parse(String value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    return value.setScale(scale, RoundingMode.DOWN).toString();
  }
}
