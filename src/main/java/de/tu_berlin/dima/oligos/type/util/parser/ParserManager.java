package de.tu_berlin.dima.oligos.type.util.parser;

import java.util.Map;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.type.util.ColumnId;

public class ParserManager {

  private static final ParserManager INSTANCE = new ParserManager();
  
  private final Map<ColumnId, Parser<?>> parsers;
  
  private ParserManager() {
    this.parsers = Maps.newHashMap();
  }
  
  public static ParserManager getInstance() {
    return INSTANCE;
  }
  
  public void register(String schema, String table, String column, Parser<?> parser) {
    ColumnId col = new ColumnId(schema, table, column);
    parsers.put(col, parser);
  }
  
  public Parser<?> getParser(String schema, String table, String column) {
    ColumnId col = new ColumnId(schema, table, column);
    return parsers.get(col);
  }
  
  public Parser<?> getParser(ColumnId columnId) {
    return parsers.get(columnId);
  }
}
