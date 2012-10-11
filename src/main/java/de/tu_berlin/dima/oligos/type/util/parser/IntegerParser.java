package de.tu_berlin.dima.oligos.type.util.parser;

public class IntegerParser implements Parser<Integer> {
  
  private static boolean isInstantiated = false;
  private static IntegerParser instance = null;
  
  private IntegerParser() {}
  
  public static synchronized IntegerParser getInstance() {
    if (isInstantiated) {
      return instance;
    } else {
      isInstantiated = true;
      instance = new IntegerParser();
      return instance;
    }
  }

  @Override
  public Integer fromString(String value) {
    return new Integer(value);
  }
  
  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
