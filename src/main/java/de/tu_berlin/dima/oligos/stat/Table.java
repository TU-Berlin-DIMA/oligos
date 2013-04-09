/*******************************************************************************
 * Copyright 2013 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tu_berlin.dima.oligos.stat;

import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;


public class Table implements Iterable<Column<?>> {
  
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
  
  public Table(final String schema, final String table, final long cardinality
      , Set<Column<?>> columns) {
    this.schema = schema;
    this.table = table;
    this.cardinality = cardinality;
    this.columns = columns;
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

  @Override
  public String toString() {
    return schema + "." + table + " " + columns.toString();
  }

  @Override
  public Iterator<Column<?>> iterator() {
    return columns.iterator();
  }
  
}
