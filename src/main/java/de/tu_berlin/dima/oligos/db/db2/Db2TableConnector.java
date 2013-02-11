package de.tu_berlin.dima.oligos.db.db2;

import java.sql.SQLException;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.TableConnector;

public class Db2TableConnector implements TableConnector {
  
  private final static String QUERY =
      "SELECT card " +
      "FROM   SYSSTAT.TABLES " +
      "WHERE  tabschema = ? AND tabname = ?";

  private final JdbcConnector connector;

  public Db2TableConnector(final JdbcConnector jdbcConnector) {
    this.connector = jdbcConnector;
  }

  @Override
  public long getCardinality(final String schema, final String table) throws SQLException {
    return connector.scalarQuery(QUERY, "card", schema, table);
  }

}
