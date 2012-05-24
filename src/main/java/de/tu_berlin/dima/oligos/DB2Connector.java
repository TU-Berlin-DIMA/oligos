package de.tu_berlin.dima.oligos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tu_berlin.dima.oligos.histogram.QHist;

public class DB2Connector {

  private static final String JDBC_STRING = "jdbc:db2://%s:%d/%s";
  private static final String HIST = "SELECT colvalue, valcount " +
  		"FROM coldist " +
  		"WHERE tabname = ? AND colname = ? AND type = ? " +
  		"ORDER BY seqno";
  @SuppressWarnings("serial")
  private static final Set<Class<?>> DISCRETE_TYPES = new HashSet<Class<?>>() {{
    add(Integer.class);
    add(Date.class);
  }};

  private Connection conn;

  private final String hostname;
  private final String database;
  private final int port;

  public DB2Connector(final String host, final String db, final int port) {
    this.hostname = host;
    this.database = db;
    this.port = port;
  }

  public String connectionString() {
    return String.format(JDBC_STRING, hostname, port, database);
  }

  public boolean connect(final String user, final String pass) {
    try {
      Class.forName("com.ibm.db2.jcc.DB2Driver");
      conn = DriverManager.getConnection(connectionString(), user, pass);

      return true;
    } catch (ClassNotFoundException e) {
      System.err.println("Could not find JDBC DB2 Driver");
      return false;
    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }
  
  /*public Class<?> getColumnType(final String tabName, final String colName) {
    
  }*/
  
  public QHist getQHistFor(String tableName, String colName) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(HIST);
    stmt.setString(1, tableName.toUpperCase());
    stmt.setString(2, colName.toUpperCase());
    stmt.setString(3, "F");
    
    // TODO Get type of column
    // TODO Get quantiles from db
    // TODO instantiate types
    // TODO build QHist
    
    return new QHist(new Comparable[0], new int[0]);
  }
  
  public static boolean isDiscreteType(Class<?> clazz) {
    return DISCRETE_TYPES.contains(clazz);
  }
}
