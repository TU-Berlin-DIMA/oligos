package de.tu_berlin.dima.oligos.type.util.operator;

import java.util.Map;

import com.google.common.collect.Maps;

public class OperatorManager {

  private final static Map<Class<?>, Operator<?>> operators = Maps.newHashMap();

  private OperatorManager(){}

  public static <T> void putOperator(Class<T> clazz, Operator<T> operator) {
    operators.put(clazz, operator);
  }

  public static <T> boolean hasOperator(Class<T> clazz) {
    return (operators.get(clazz) != null) ? true : false;
  }
 
  @SuppressWarnings("unchecked")
  public static <T> Operator<T> getOperator(Class<T> clazz) {
    Operator<T> operator = (Operator<T>) operators.get(clazz);
    if (operator == null) {
      throw new RuntimeException("No operator found for " + clazz.getSimpleName());
    }
    return operator;
  }

}
