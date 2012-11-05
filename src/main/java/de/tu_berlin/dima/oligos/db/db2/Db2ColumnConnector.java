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
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class Db2ColumnConnector<T> implements ColumnConnector<T> {
  
  private final static String TYPE_QUERY =
      "SELECT typename, length, scale " +
      "FROM   SYSCAT.COLUMNS " +
      "WHERE  tabschema = ? AND tabname = ? AND colname = ?";
  private final static String ENUMERATED_QUERY =
      "SELECT R.num_most_frequent, S.colcard " +
      "FROM   (SELECT COUNT(*) as num_most_frequent " +
      "        FROM SYSSTAT.COLDIST " +
      "        WHERE tabschema = ?" +
      "          AND tabname = ? " +
      "          AND colname = ? " +
      "          AND type = 'F' " +
      "          AND colvalue is not null) as R, " +
      "       (SELECT colcard " +
      "        FROM   SYSSTAT.COLUMNS " +
      "        WHERE  tabschema = ? AND tabname = ? AND colname = ?) as S";
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

  public Db2ColumnConnector(final JdbcConnector jdbcConnector, final String schema
      , final String table, final String column, final Parser<T> parser) {
    this.connector = jdbcConnector;
    this.schema = schema;
    this.table = table;
    this.column = column;
    this.parser = parser;
  }

  @Override
  public boolean hasStatistics() throws SQLException {
    ResultSet result = connector.executeQuery(DOMAIN_QUERY, schema, table, column);
    if (result.next()) {
      long card = result.getLong("COLCARD");
      return (card != -1) ? true : false; 
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
  }

  @Override
  public TypeInfo getType() throws SQLException {
    ResultSet result = connector.executeQuery(TYPE_QUERY, schema, table, column);
    if (result.next()) {
      String typeName = result.getString("typename");
      int length = result.getInt("length");
      int scale = result.getInt("scale");
      return new TypeInfo(typeName, length, scale);
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
  }

  @Override
  public boolean isEnumerated() throws SQLException {
    ResultSet result = connector.executeQuery(ENUMERATED_QUERY, schema, table, column
        , schema, table, column);
    if (result.next()) {
      long colCard = result.getLong("colcard");
      int numMostFreq = result.getInt("num_most_frequent");
      return colCard <= numMostFreq;
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
  }

  @Override
  public boolean isNullable() throws SQLException {
    ResultSet result = connector.executeQuery(DOMAIN_QUERY, schema, table, column);
    if (result.next()) {
      boolean nullable = result.getString("nulls").charAt(0) == 'Y' ? true : false;
      return nullable;
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
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
    if (!isNullable()) {
      constraints.add(Constraint.NOT_NULL);
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

  @Override
  public Map<T, Long> getMostFrequentValues() throws SQLException {
    Map<T, Long> mostFrequentValues = Maps.newLinkedHashMap();
    ResultSet result = connector.executeQuery(MOST_FREQUENT_QUERY, schema, table, column);
    while (result.next()) {
      T value = parser.fromString(result.getString("colvalue"));
      long count = result.getLong("valcount");
      mostFrequentValues.put(value, count);
    }
    return mostFrequentValues;
  }

  @Override
  public Map<T, Long> getHistogram() throws SQLException {
    Map<T, Long> quantileHistogram = Maps.newLinkedHashMap();
    ResultSet result = connector.executeQuery(QUANTILE_HISTOGRAM_QUERY, schema, table, column);
    while (result.next()) {
      T value = parser.fromString(result.getString("colvalue"));
      long count = result.getLong("valcount");
      quantileHistogram.put(value, count);
    }
    return quantileHistogram;
  }

}