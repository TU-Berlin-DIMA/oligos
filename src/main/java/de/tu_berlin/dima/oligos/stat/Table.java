package de.tu_berlin.dima.oligos.stat;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;


public class Table {
  
  private final String schema;
  private final String table;
  private final long cardinality;
  private final Set<Column<?>> columns;
  
  public Table(final String schema, final String table, final long cardinality) {
    this.schema = schema;
    this.table = table;
    this.cardinality = cardinality;
    this.columns = Sets.newLinkedHashSet();
  }
  
  public String getSchema() {
    return schema;
  }

  public String getTable() {
    return table;
  }
  
  public long getCardinality() {
    return cardinality;
  }
  
  public Set<Column<?>> getColumns() {
    return columns;
  }
  
  public void addColumn(Column<?> column) {
    columns.add(column);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schema, table, cardinality, columns);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Table other = (Table) obj;
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
  
}
