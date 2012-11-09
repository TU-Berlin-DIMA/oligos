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

  public void addTableProfiler(final TableProfiler tableProfiler) {
    tableProfilers.add(tableProfiler);
  }
  
  public Schema profile() {
    Set<Table> tables = getTableStatistics();
    Set<Quartet<String, String, String, String>> references = getReferences();
    return new Schema(schema, tables, references);
  }
  
  private Set<Table> getTableStatistics() {
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
