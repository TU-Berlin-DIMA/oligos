package de.tu_berlin.dima.oligos;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SparseSchema {
  
  private final Map<String, Map<String, Set<String>>> schemas;
  
  private SparseSchema(final Map<String, Map<String, Set<String>>> schemas) {
    this.schemas = schemas;
  }

  public Set<String> schemas() {
    return schemas.keySet();
  }

  public Set<String> emptySchemas() {
    Set<String> emptySchemas = Sets.newLinkedHashSet();
    for (String schema : schemas()) {
      if (tablesIn(schema).isEmpty()) {
        emptySchemas.add(schema);
      }
    }
    return emptySchemas;
  }

  public Set<String> tablesIn(final String schema) {
    if (schemas().contains(schema)) {
      return schemas.get(schema).keySet();
    }
    else {
      return Sets.newLinkedHashSet();
    }
  }

  public Set<String> emptyTablesIn(final String schema) {
    Set<String> emptyTables = Sets.newLinkedHashSet();
    for (String table : tablesIn(schema)) {
      if (schemas.get(schema).get(table).isEmpty()) {
        emptyTables.add(table);
      }
    }
    return emptyTables;
  }

  public Set<String> columnsIn(final String schema, final String table) {
    if (tablesIn(schema).contains(table)) {
      return schemas.get(schema).get(table);
    }
    else {
      return Sets.newLinkedHashSet();
    }
  }

  /*@Override
  public Iterator<ColumnId> iterator() {
    return new Iterator<ColumnId>() {

      private Iterator<String> schemaIter = schemas().iterator();
      private String currentSchema = "";
      private Iterator<String> tableIter;
      private String currentTable;
      private Iterator<String> columnIter;
      private String currentColumn;
      private boolean hasNext = schemaIter.hasNext();

      @Override
      public boolean hasNext() {
        return hasNext;
      }

      @Override
      public ColumnId next() {
        // the current table has more columns
        if (columnIter.hasNext()) {
          currentColumn = columnIter.next();
        } else {
          if (tableIter.hasNext()) {
            
          }
          else {
            
          }
        }
        return new ColumnId(currentSchema, currentTable, currentColumn);
      }

      @Override
      public void remove() {
        // TODO Auto-generated method stub
        
      }
    };
  }*/

  /**
   * Builder class for creating immutable {@link SparseSchema} instances.
   * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
   *
   */
  public static class SparseSchemaBuilder {
    private final Map<String, Map<String, Set<String>>> schemas = Maps.newLinkedHashMap();

    public Map<String, Map<String, Set<String>>> getSparseSchema() {
      return schemas;
    }

    /**
     * Add the given schema to the input schema definition.
     * @param schema
     *  Name of the schema
     * @return
     *  Tables associated with the given schema.
     */
    public SparseSchemaBuilder addSchema(final String schema) {
      Preconditions.checkNotNull(schema);
      Map<String, Set<String>> tables = schemas.get(schema);
      if (tables == null) {
        tables = Maps.newLinkedHashMap();
      }
      schemas.put(schema, tables);
      return this;
    }

    /**
     * Add the a table to the given schema. If the schema already exists the
     * given table is added to it, the schema is created otherwise.
     * @param schema
     *  Name of the schema
     * @param table
     *  Name of the table
     */
    public SparseSchemaBuilder addTable(final String schema, final String table) {
      Preconditions.checkNotNull(table);
      addSchema(schema);
      Map<String, Set<String>> tables = schemas.get(schema);
      if (!tables.containsKey(table)) {
        tables.put(table, Sets.<String>newLinkedHashSet());
      }
      return this;
    }

    /**
     * Adds a column to the given in table within the given schema.
     * @param schema
     *  Name of the schema
     * @param table
     *  Name of the table
     * @param column
     *  Name of the column
     */
    public SparseSchemaBuilder addColumn(final String schema, final String table, final String column) {
      Preconditions.checkNotNull(column);
      addTable(schema, table);
      Set<String> columns = schemas.get(schema).get(table);
      if (!columns.contains(column)) {
        columns.add(column);
      }
      return this;
    }

    public SparseSchema build() {
      return new SparseSchema(schemas);
    }

    @Override
    public String toString() {
      return schemas.toString();
    }
  }
}
