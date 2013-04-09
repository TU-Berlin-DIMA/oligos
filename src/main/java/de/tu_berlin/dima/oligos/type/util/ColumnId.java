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
package de.tu_berlin.dima.oligos.type.util;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

public class ColumnId implements Comparable<ColumnId> {
  private final String schema;
  private final String table;
  private final String column;
  
  public ColumnId(final String schema, final String table, final String column) {
    this.schema = schema;
    this.table = table;
    this.column = column;
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

  @Override
  public int compareTo(ColumnId other) {
    return ComparisonChain.start()
        .compare(this.schema, other.schema)
        .compare(this.table, other.table)
        .compare(this.column, other.column)
        .result();
  }

  public static class ColumnIdBuilder {
    public String schema;
    public String table;
    public String column;

    public ColumnIdBuilder setSchema(final String schema) {
      this.schema = schema;
      return this;
    }

    public ColumnIdBuilder setTable(final String table) {
      this.table = table;
      return this;
    }

    public ColumnIdBuilder setColumn(final String column) {
      this.column = column;
      return this;
    }

    public ColumnId build() {
      return new ColumnId(schema, table, column);
    }
  }
}
