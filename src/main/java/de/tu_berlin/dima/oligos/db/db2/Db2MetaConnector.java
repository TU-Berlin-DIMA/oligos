package de.tu_berlin.dima.oligos.db.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.MetaConnector;
import de.tu_berlin.dima.oligos.exception.ColumnDoesNotExistException;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;

public class Db2MetaConnector implements MetaConnector {

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

  private final static String DOMAIN_QUERY =
      "SELECT colcard " +
      "FROM   SYSCAT.COLUMNS " +
      "WHERE  tabschema = ? AND tabname = ? AND colname = ?";

  private final static String TYPE_QUERY =
      "SELECT typename, length, scale " +
      "FROM   SYSCAT.COLUMNS " +
      "WHERE  tabschema = ? AND tabname = ? AND colname = ?";

  private final JdbcConnector connector;

  public Db2MetaConnector(final JdbcConnector jdbcConnector) {
    this.connector = jdbcConnector;
  }

  @Override
  public boolean hasColumn(final ColumnId columnId) throws SQLException {
    String schema = columnId.getSchema();
    String table = columnId.getTable();
    String column = columnId.getColumn();
    return hasColumn(schema, table, column);
  }

  @Override
  public boolean hasColumn(final String schema, final String table, final String column)
      throws SQLException {
    return connector.checkColumn(schema, table, column);
  }

  @Override
  public boolean hasStatistics(ColumnId columnId) throws SQLException {
    String schema = columnId.getSchema();
    String table = columnId.getTable();
    String column = columnId.getColumn();
    return hasStatistics(schema, table, column);
  }

  @Override
  public boolean hasStatistics(String schema, String table, String column)
      throws SQLException {
    ResultSet result = connector.executeQuery(DOMAIN_QUERY, schema, table, column);
    if (result.next()) {
      long card = result.getLong("COLCARD");
      return (card != -1) ? true : false; 
    } else {
      throw new ColumnDoesNotExistException(schema, table, column);
    }
  }

  @Override
  public boolean isEnumerated(ColumnId columnId) throws SQLException {
    String schema = columnId.getSchema();
    String table = columnId.getTable();
    String column = columnId.getColumn();
    return isEnumerated(schema, table, column);
  }

  @Override
  public boolean isEnumerated(String schema, String table, String column)
      throws SQLException {
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
  public TypeInfo getColumnType(ColumnId columnId) throws SQLException {
    String schema = columnId.getSchema();
    String table = columnId.getTable();
    String column = columnId.getColumn();
    return getColumnType(schema, table, column);
  }

  @Override
  public TypeInfo getColumnType(final String schema, final String table, final String column)
      throws SQLException {
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

}
