package de.tu_berlin.dima.oligos;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.type.util.ColumnId;

public class InputSchema implements Iterable<ColumnId> {
  
  private final Map<String, Map<String, Set<String>>> schemas;
  
  public InputSchema() {
    this.schemas = Maps.newLinkedHashMap();
  }

  public void addColumn(final ColumnId columnId) {
    addColumn(columnId.getSchema(), columnId.getTable(), columnId.getColumn());
  }

  public void addColumn(final String schema, final String table, final String column) {
    Map<String, Set<String>> tables = schemas.get(schema);
    if (tables == null) {
      tables = Maps.newLinkedHashMap();
      schemas.put(schema, tables);
    }
    Set<String> columns = tables.get(table);
    if (columns == null) {
      columns = Sets.newLinkedHashSet();
      tables.put(table, columns);
    }
    columns.add(column);
  }

  @Override
  public Iterator<ColumnId> iterator() {
    return new Iterator<ColumnId>() {

      private Iterator<String> schemaIter = schemas.keySet().iterator();
      private String currentSchema = schemaIter.next();
      private Iterator<String> tableIter = schemas.get(currentSchema).keySet().iterator();
      private String currentTable = tableIter.next();
      private Iterator<String> columnIter =
          schemas.get(currentSchema).get(currentTable).iterator();

      @Override
      public boolean hasNext() {
        if (columnIter.hasNext()) {
          return true;
        } else {
          if (tableIter.hasNext()) {
            return true;
          } else if (schemaIter.hasNext()) {
            return true;
          } else {
            return false;
          }
        }
      }

      @Override
      public ColumnId next() {
        if (columnIter.hasNext()) {
          return new ColumnId(currentSchema, currentTable, columnIter.next());
        } else if (hasNext()) {
          if (tableIter.hasNext()) {
            currentTable = tableIter.next();
            columnIter = schemas.get(currentSchema).get(currentTable).iterator();
          } else {
            currentSchema = schemaIter.next();
            tableIter = schemas.get(currentSchema).keySet().iterator();
            currentTable = tableIter.next();
            columnIter = schemas.get(currentSchema).get(currentTable).iterator();
          }
          return new ColumnId(currentSchema, currentTable, columnIter.next());
        } else {
          throw new NoSuchElementException();
        }
      }

      @Override
      public void remove() {
        columnIter.remove();
      }
    };  
  }

  public Set<String> schemas() {
    return schemas.keySet();
  }

  public Set<String> tables(final String schema) {
    if (schemas.containsKey(schema)) {
      return schemas.get(schema).keySet();
    } else {
      return Sets.newLinkedHashSet();
    }
  }

  public Set<String> columns(final String schema, final String table) {
    if (schemas.containsKey(schema)) {
      Map<String, Set<String>> tables = schemas.get(schema);
      if (tables.containsKey(table)) {
        return tables.get(table);
      }
    }
    return Sets.newLinkedHashSet();
  }
}
