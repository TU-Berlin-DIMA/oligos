package de.tu_berlin.dima.oligos.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public class JdbcConnector {
  
  public final static String IBM_JDBC_V4 = "db2";

  private final static String JDBC_STRING = "jdbc:%s://%s:%d/%s";

  private final String hostname;
  private final int port;
  private final String database;
  private final String dbDriver;

  private Connection connection;
  private DatabaseMetaData metaData;
  
  public JdbcConnector(final String hostname, final int port, final String database
      , final String dbDriver) {
    this.hostname = hostname;
    this.port = port;
    this.database = database;
    this.dbDriver = dbDriver;
    this.metaData = null;
  }

  public String getConnectionString() {
    return String.format(JDBC_STRING, dbDriver, hostname, port, database);
  }

  public void connect(final String user, final String pass) throws SQLException {
    connection = DriverManager.getConnection(getConnectionString(), user, pass);
    metaData = connection.getMetaData();
  }

  public void close() throws SQLException {
    connection.close();
  }

  public Collection<String> getSchemas() throws SQLException {
    List<String> schemas = Lists.newArrayList();
    ResultSet result = metaData.getSchemas();
    while (result.next()) {
      String schema = result.getString("TABLE_SCHEM");
      schemas.add(schema);
    }
    return schemas;
  }

  public Collection<String> getTables(final String schema) throws SQLException {
    ResultSet result = metaData.getTables(null, schema, null, null);
    List<String> tables = Lists.newArrayList();
    while (result.next()) {
      String table = result.getString("TABLE_NAME");
      tables.add(table);
    }
    return tables;
  }

  public Collection<String> getColumns(final String schema, final String table) throws SQLException {
    ResultSet result = metaData.getColumns(null, schema, table, null);
    List<String> columns = Lists.newArrayList();
    while (result.next()) {
      String column = result.getString("COLUMN_NAME");
      columns.add(column);
    }
    return columns;
  }

  public boolean checkSchema(final String schema) throws SQLException {
    ResultSet result = metaData.getSchemas(null, schema);
    if (result.next()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean checkTable(final String schema, final String table) throws SQLException {
    ResultSet result = metaData.getTables(null, schema, table, null);
    if (result.next()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean checkColumn(final String schema, final String table, final String column) 
      throws SQLException {
    ResultSet result = metaData.getColumns(null, schema, table, column);
    if (result.next()) {
      return true;
    } else {
      return false;
    }
  }
  
  public ResultSet executeQuery(final String query, final Object... parameters) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(query);
    for (int i = 1; i <= parameters.length; i++) {
      stmt.setObject(i, parameters[i-1]);
    }
    ResultSet result = stmt.executeQuery();
    return result;
  }
}
