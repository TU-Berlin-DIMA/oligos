package de.tu_berlin.dima.oligos.type.util.operator;

import java.util.Map;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.type.util.ColumnId;

public class OperatorManager {

  private final Map<ColumnId, Operator<?>> operators;
  
  public OperatorManager() {
    this.operators = Maps.newHashMap();
  }

  public void register(ColumnId columnId, Operator<?> operator) {
    operators.put(columnId, operator);
  }
  public void register(String schema, String table, String column, Operator<?> operator) {
    ColumnId col = new ColumnId(schema, table, column);
    register(col, operator);
  }
  public Operator<?> getOperator(String schema, String table, String column) {
    ColumnId col = new ColumnId(schema, table, column);
    return operators.get(col);
  }
  
  public Operator<?> getOperator(ColumnId columnId) {
    return operators.get(columnId);
  }
}
