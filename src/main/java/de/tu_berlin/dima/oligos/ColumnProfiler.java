package de.tu_berlin.dima.oligos;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.db.DB2Connector;
import de.tu_berlin.dima.oligos.type.util.Parser;

public class ColumnProfiler<T> {

  private final DB2Connector connector;
  private final String table;
  private final String column;
  //private final Operator<T> operator;
  private final Parser<T> parser;
  
  public ColumnProfiler(DB2Connector connector, Parser<T> parser, String table, String column) {
    this.connector = connector;
    this.table = table;
    this.column = column;
    //this.operator = operator;
    this.parser = parser;
  }
  
  public Map<T, Long> getMostFrequentValues() {
    Map<T, Long> mostFrequent = Maps.newLinkedHashMap();
    try {
      Map<String, Long> stringMostFrequent = connector.getMostFrequentValues(table, column);
      for (Entry<String, Long> entry : stringMostFrequent.entrySet()) {
        T key = parser.fromString(entry.getKey());
        mostFrequent.put(key, entry.getValue());
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return mostFrequent;
  }
  
  public void printMostFrequentValues() {
    Map<T, Long> mostFrequent = getMostFrequentValues();
    for (Entry<T, Long> e : mostFrequent.entrySet()) {
      System.out.println(parser.toString(e.getKey()) + "\t" + e.getValue());
    }
  }
}
