package de.tu_berlin.dima.oligos.type.util.operator;

import java.util.Map;

import com.google.common.collect.Maps;

public class OperatorManager {

  private final static Map<Class<?>, Operator<?>> operators = Maps.newHashMap();

  private OperatorManager(){}

  public static <T> void putOperator(Class<T> clazz, Operator<T> operator) {
    operators.put(clazz, operator);
  }
 
  @SuppressWarnings("unchecked")
  public static <T> Operator<T> getOperator(Class<T> clazz) {
    return (Operator<T>) operators.get(clazz);
  }

}
