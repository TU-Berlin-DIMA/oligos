package de.tu_berlin.dima.oligos.type.util.parser;

import java.util.Map;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.type.util.ColumnId;

public class ParserManager {

  private final Map<ColumnId, Parser<?>> parsers;
  
  public ParserManager() {
    this.parsers = Maps.newHashMap();
  }
  
  public void register(ColumnId columnId, Parser<?> parser) {
    parsers.put(columnId, parser);
  }

  public void register(String schema, String table, String column, Parser<?> parser) {
    ColumnId col = new ColumnId(schema, table, column);
    register(col, parser);
  }
  
  public Parser<?> getParser(String schema, String table, String column) {
    ColumnId col = new ColumnId(schema, table, column);
    return getParser(col);
  }
  
  public Parser<?> getParser(ColumnId columnId) {
    return parsers.get(columnId);
  }
}
