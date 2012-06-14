package de.tu_berlin.dima.oligos.type;

import com.google.common.collect.ComparisonChain;


public class IntegerType implements Type<IntegerType> {
  
  private final int value;
  
  public IntegerType(int value) {
    this.value = value;
  }
  
  public IntegerType(String value) {
    this.value = new Integer(value);
  }

  @Override
  public IntegerType increment() {
    return new IntegerType(value + 1);
  }

  @Override
  public IntegerType decrement() {
    return new IntegerType(value - 1);
  }
  
  @Override
  public int difference(IntegerType other) {
   return Math.abs(other.value - this.value); 
  }

  @Override
  public IntegerType parse(String value) {
    return new IntegerType(new Integer(value));
  }

  @Override
  public String toString() {
    return value + "";
  }

  @Override
  public int compareTo(IntegerType o) {
    return ComparisonChain.start().compare(this.value, o.value).result();
  }

}
