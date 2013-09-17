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
package de.tu_berlin.dima.oligos.profiler;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.TableConnector;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.Table;

public class TableProfiler implements Profiler<Table> {

  private final String schema;
  private final String table;
  private final TableConnector connector;
  private final Set<ColumnProfiler<?>> columnProfilers;
  
  public TableProfiler(final String schema, final String table, final TableConnector connector) {
    this(schema, table, connector, new LinkedHashSet<ColumnProfiler<?>>());
  }
  
  public TableProfiler(final String schema, final String table, final TableConnector connector
      , final Set<ColumnProfiler<?>> columnProfilers) {
    this.schema = schema;
    this.table = table;
    this.connector = connector;
    this.columnProfilers = columnProfilers;
  }
  
  public void addColumnProfiler(final ColumnProfiler<?> columnProfiler) {
    columnProfilers.add(columnProfiler);
  }
  
  public Table profile() {
    try {
      long cardinality = connector.getCardinality(schema, table);
      Set<Column<?>> columns = getColumnStatistics();
      return new Table(schema, table, cardinality, columns);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  private Set<Column<?>> getColumnStatistics() {
    Set<Column<?>> columns = Sets.newLinkedHashSet();
  	for (ColumnProfiler<?> profiler : this.columnProfilers) {
      Column<?> column = profiler.profile();
      columns.add(column);
    }
    return columns;
  }

}
