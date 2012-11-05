package de.tu_berlin.dima.oligos.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcConnector {
  
  public final static String IBM_JDBC_V4 = "com.ibm.db2.jcc.DB2Driver";

  private final static String JDBC_STRING = "jdbc:db2://%s:%d/%s";

  private final String hostname;
  private final int port;
  private final String database;
  private final String dbDriver;

  private Connection connection;
  
  public JdbcConnector(final String hostname, final int port, final String database
      , final String dbDriver) {
    this.hostname = hostname;
    this.port = port;
    this.database = database;
    this.dbDriver = dbDriver;
  }

  public String getConnectionString() {
    return String.format(JDBC_STRING, hostname, port, database);
  }

  public boolean connect(final String user, final String pass) throws SQLException {
    try {
      Class.forName(dbDriver);
      connection = DriverManager.getConnection(getConnectionString(), user, pass);
      return true;
    } catch (ClassNotFoundException e) {
      System.err.println("Could not find JDBC DB2 Driver " + dbDriver);
      return false;
    } 
  }

  public void close() throws SQLException {
    connection.close();
  }

  public boolean checkColumn(String table, String column) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet result = metaData.getColumns(null, null, table.toUpperCase(), column.toUpperCase());
    if (result.next()) {
      return true;
    } else {
      return false;
    }
  }
  
  public ResultSet executeQuery(final String query, final Object... parameters) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(query);
    for (int i = 1; i < parameters.length; i++) {
      stmt.setObject(i, parameters[i-1]);
    }
    ResultSet result = stmt.executeQuery();
    return result;
  }
}
