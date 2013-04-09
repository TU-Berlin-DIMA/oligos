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
package de.tu_berlin.dima.oligos;

import java.util.Iterator;
import java.util.Set;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.type.util.ColumnId;

public class DenseSchema implements Iterable<ColumnId> {

  private final Set<ColumnId> columns;

  public DenseSchema() {
    columns = Sets.newLinkedHashSet();
  }

  public DenseSchema(ColumnId... columns) {
    this();
    for (ColumnId column : columns) {
      addColumn(column);
    }
  }

  public void addColumn(ColumnId column) {
    Preconditions.checkArgument(
        column.getSchema() != null &&
        column.getTable() != null &&
        column.getClass() != null);
    Preconditions.checkArgument(
        !column.getSchema().isEmpty() &&
        !column.getTable().isEmpty() &&
        !column.getColumn().isEmpty());
    columns.add(column);
  }

  public Set<String> schemas() {
    Set<String> schemas = Sets.newLinkedHashSet();
    for (ColumnId column : columns) {
      String schema = column.getSchema();
      schemas.add(schema);
    }
    return schemas;
  }

  public Set<String> tablesIn(final String schema) {
    Set<String> tables = Sets.newLinkedHashSet();
    for (ColumnId column : columns) {
      if (column.getSchema().equals(schema)) {
        String table = column.getTable();
        tables.add(table);
      }
    }
    return tables;
  }

  public Set<String> columnsIn(final String schema, final String table) {
    Set<String> cols = Sets.newLinkedHashSet();
    for (ColumnId column : columns) {
      if (column.getSchema().equals(schema) && column.getTable().equals(table)) {
        String col = column.getColumn();
        cols.add(col);
      }
    }
    return cols;
  }

  @Override
  public Iterator<ColumnId> iterator() {
    return columns.iterator();
  }

  @Override
  public String toString() {
    return columns.toString();
  }
}
