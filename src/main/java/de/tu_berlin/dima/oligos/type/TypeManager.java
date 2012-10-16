package de.tu_berlin.dima.oligos.type;

import java.util.Map;

import com.google.common.collect.Maps;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class TypeManager {
  
  private static final TypeManager INSTANCE = new TypeManager();
  
  private final Map<Column, Parser<?>> parsers;
  
  private TypeManager() {
    this.parsers = Maps.newHashMap();
  }
  
  public static TypeManager getInstance() {
    return INSTANCE;
  }
  
  public void registerColumn(String schema, String table, String column, Parser<?> parser) {
    Column col = new Column(schema, table, column);
    parsers.put(col, parser);
  }
  
  public Parser<?> getParser(String schema, String table, String column) {
    Column col = new Column(schema, table, column);
    return parsers.get(col);
  }
  
  static class Column {
    private final String schema;
    private final String table;
    private final String column;
    
    public Column(final String schema, final String table, final String column) {
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
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Column other = (Column) obj;
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
      final int prime = 31;
      int result = 1;
      result = prime * result + ((column == null) ? 0 : column.hashCode());
      result = prime * result + ((schema == null) ? 0 : schema.hashCode());
      result = prime * result + ((table == null) ? 0 : table.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return schema + "." + table + "." + column;
    }
  }

}
