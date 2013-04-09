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
import java.util.Set;

import org.javatuples.Quartet;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.SchemaConnector;
import de.tu_berlin.dima.oligos.stat.Schema;
import de.tu_berlin.dima.oligos.stat.Table;

public class SchemaProfiler implements Profiler<Schema> {
  
  private final String schema;
  private final SchemaConnector connector;
  private final Set<TableProfiler> tableProfilers;
  
  public SchemaProfiler(final String schema, final SchemaConnector connector
      , final Set<TableProfiler> tableProfilers) {
    this.schema = schema;
    this.connector = connector;
    this.tableProfilers = tableProfilers;
  }

  public SchemaProfiler(final String schema, final SchemaConnector connector) {
    this.schema = schema;
    this.connector = connector;
    this.tableProfilers = Sets.newLinkedHashSet();
  }

  public void add(final TableProfiler tableProfiler) {
    tableProfilers.add(tableProfiler);
  }
  
  public Schema profile() {
    Set<Table> tables = getTables();
    Set<Quartet<String, String, String, String>> references = getReferences();
    return new Schema(schema, tables, references);
  }
  
  private Set<Table> getTables() {
    Set<Table> tables = Sets.newLinkedHashSet();
    for (TableProfiler profiler : tableProfilers) {
      Table table = profiler.profile();
      tables.add(table);
    }
    return tables;
  }

  private Set<Quartet<String, String, String, String>> getReferences() {
    try {
      Set<Quartet<String, String, String, String>> refs = connector.getReferences(schema);
      return refs;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
