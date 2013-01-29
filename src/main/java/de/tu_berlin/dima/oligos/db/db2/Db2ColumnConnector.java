package de.tu_berlin.dima.oligos.db.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.exception.ColumnDoesNotExistException;
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
    ResultSet result = connector.executeQuery(CONSTRAINT_QUERY, schema, table, column);
    if (result.next()) {
      char con = result.getString("TYPE").charAt(0);
      if (con == 'U') {
        constraints.add(Constraint.UNIQUE);
      } else if (con == 'P') {
        constraints.add(Constraint.PRIMARY_KEY);
      } else if (con == 'F') {
        constraints.add(Constraint.FOREIGN_KEY);
      }
     }
    return constraints;
  }

  @Override
  public long getNumNulls() throws SQLException {
    ResultSet result = connector.executeQuery(DOMAIN_QUERY, schema, table, column);
    if (result.next()) {
      return result.getLong("numnulls");
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
  }

  @Override
  public long getCardinality() throws SQLException {
    ResultSet result = connector.executeQuery(DOMAIN_QUERY, schema, table, column);
    if (result.next()) {
      return result.getLong("colcard");
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
  }

  public T getMin() throws SQLException {
    ResultSet result = connector.executeQuery(DOMAIN_QUERY, schema, table, column);
    if (result.next()) {
      T min = parser.fromString(result.getString("low2key").replaceAll("'", ""));
      return min;
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
  }

  public T getMax() throws SQLException {
    ResultSet result = connector.executeQuery(DOMAIN_QUERY, schema, table, column);
    if (result.next()) {
      T max = parser.fromString(result.getString("high2key").replaceAll("'", ""));
      return max;
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
  }

  @Override
  public Map<T, Long> getMostFrequentValues() throws SQLException {
    Map<T, Long> mostFrequentValues = Maps.newLinkedHashMap();
    ResultSet result = connector.executeQuery(MOST_FREQUENT_QUERY, schema,
        table, column);
    while (result.next()) {
      String colvalue = result.getString("colvalue");
      if (colvalue != null) {
        T value = parser.fromString(colvalue.replaceAll("'", ""));
        long count = result.getLong("valcount");
        mostFrequentValues.put(value, count);
      }
    }
    return mostFrequentValues;
  }

  @Override
  public Map<T, Long> getHistogram() throws SQLException {
    Map<T, Long> quantileHistogram = Maps.newLinkedHashMap();
    ResultSet result = connector.executeQuery(QUANTILE_HISTOGRAM_QUERY, schema, table, column);
    while (result.next()) {
      String colvalue = result.getString("colvalue");
      if (colvalue != null) {
        T value = parser.fromString(colvalue.replaceAll("'", ""));
        long count = result.getLong("valcount");
        quantileHistogram.put(value, count);
      }
    }
    return quantileHistogram;
  }
}
