package de.tu_berlin.dima.oligos.db.db2;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class Db2ColumnConnector<T> implements ColumnConnector<T> {
  
  private final static String CONSTRAINT_QUERY =
      "SELECT type " +
      "FROM   SYSCAT.TABCONST tc, SYSCAT.KEYCOLUSE kcu " +
      "WHERE  tc.constname = kcu.constname " +
      "  AND  tc.tabschema = ? AND tc.tabname = ? AND kcu.colname = ?";
  private final static String DOMAIN_QUERY =
      "SELECT low2key, high2key, numnulls, colcard, nulls, typename, length, scale " +
      "FROM   SYSCAT.COLUMNS " +
      "WHERE  tabschema = ? AND tabname = ? AND colname = ?";
  private final static String MOST_FREQUENT_QUERY =
      "SELECT colvalue, valcount " +
      "FROM   SYSSTAT.COLDIST " +
      "WHERE  tabschema = ? AND tabname = ? AND colname = ? AND type = 'F' " +
      "ORDER BY seqno";
  private final static String QUANTILE_HISTOGRAM_QUERY =
      "SELECT colvalue, valcount " +
      "FROM   SYSSTAT.COLDIST " +
      "WHERE  tabschema = ? AND tabname = ? AND colname = ? AND type = 'Q' " +
      "ORDER BY seqno";

  private final JdbcConnector connector;
  private final String schema;
  private final String table;
  private final String column;
  private final Parser<T> parser;
  
  public Db2ColumnConnector(final JdbcConnector jdbcConnector, final ColumnId columnId
      , final Parser<T> parser) {
    this(jdbcConnector, columnId.getSchema(), columnId.getTable(), columnId.getColumn(), parser);
  }

  public Db2ColumnConnector(final JdbcConnector jdbcConnector, final String schema
      , final String table, final String column, final Parser<T> parser) {
    this.connector = jdbcConnector;
    this.schema = schema;
    this.table = table;
    this.column = column;
    this.parser = parser;
  }

  @Override
  public Set<Constraint> getConstraints() throws SQLException {
    Set<Constraint> constraints = Sets.newHashSet();
    String con = connector.scalarQuery(CONSTRAINT_QUERY, "type", schema,
        table, column);
    if (con != null) {
      if (con.equals("U")) {
        constraints.add(Constraint.UNIQUE);
      } else if (con.equals("P")) {
        constraints.add(Constraint.PRIMARY_KEY);
      } else if (con.equals("F")) {
        constraints.add(Constraint.FOREIGN_KEY);
      }
    }
    return constraints;
  }

  @Override
  public long getNumNulls() throws SQLException {
    return connector.<Long>scalarQuery(DOMAIN_QUERY, "numnulls", schema, table, column);
  }

  @Override
  public long getCardinality() throws SQLException {
    return connector.<Long>scalarQuery(DOMAIN_QUERY, "colcard", schema, table, column);
  }

  public T getMin() throws SQLException {
    String minStr = connector.scalarQuery(DOMAIN_QUERY, "low2key", schema, table, column);
    return parser.fromString(minStr);
  }

  public T getMax() throws SQLException {
    String minStr = connector.scalarQuery(DOMAIN_QUERY, "high2key", schema, table, column);
    return parser.fromString(minStr);
  }

  @Override
  public Map<T, Long> getMostFrequentValues() throws SQLException {
    return connector.histogramQuery(
        MOST_FREQUENT_QUERY, "colvalue", "valcount", parser, schema, table, column);
  }

  @Override
  public Map<T, Long> getHistogram() throws SQLException {
    Map<T, Long> rawHist = connector.histogramQuery(
        QUANTILE_HISTOGRAM_QUERY, "colvalue", "valcount", parser, schema, table, column);
    Map<T, Long> normHist = Maps.newLinkedHashMap();
    long lastFreq = 0L;
    for (Entry<T, Long> e : rawHist.entrySet()) {
      T value = e.getKey();
      long cumCount = e.getValue();
      normHist.put(value, cumCount - lastFreq);
      lastFreq = cumCount;
    }
    return normHist;
  }
}
