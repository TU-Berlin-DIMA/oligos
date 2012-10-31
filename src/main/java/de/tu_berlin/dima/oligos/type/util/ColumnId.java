package de.tu_berlin.dima.oligos.type.util;

import com.google.common.base.Objects;

public class ColumnId {
  private final String schema;
  private final String table;
  private final String column;
  
  public ColumnId(final String schema, final String table, final String column) {
    this.schema = schema.toLowerCase();
    this.table = table.toLowerCase();
    this.column = column.toLowerCase();
  }
  
  public String getSchema() {
    return schema;
  }
  
  public String getTable() {
    return table;
  }
  
  public String getColumn() {
    return column;
  }
  
  public String getQualifiedName() {
    return getQualifiedName('.');
  }
  
  public String getQualifiedName(char delimiter) {
    StringBuilder strBld = new StringBuilder();
    if (!schema.isEmpty()) {
      strBld.append(schema);
      strBld.append(delimiter);
    }
    strBld.append(table);
    strBld.append(delimiter);
    strBld.append(column);
    return strBld.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ColumnId other = (ColumnId) obj;
    if (column == null) {
      if (other.column != null)
        return false;
    } else if (!column.equals(other.column))
      return false;
    if (schema == null) {
      if (other.schema != null)
        return false;
    } else if (!schema.equals(other.schema))
      return false;
    if (table == null) {
      if (other.table != null)
        return false;
    } else if (!table.equals(other.table))
      return false;
    return true;
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(schema, table, column);
  }

  @Override
  public String toString() {
    return schema + "." + table + "." + column;
  }
}
