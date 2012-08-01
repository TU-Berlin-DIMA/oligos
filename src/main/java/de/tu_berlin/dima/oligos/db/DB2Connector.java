package de.tu_berlin.dima.oligos.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;

public class DB2Connector {
  
  private final static String JDBC_STRING = "jdbc:db2://%s:%d/%s";
  private final static String TYPE_QUERY =
      "SELECT typename, length, scale " +
      "FROM   SYSCAT.COLUMNS " +
      "WHERE  tabname = ? AND colname = ?";
  private final static String ENUMERATED_QUERY =
      "SELECT R.num_most_frequent, S.colcard " +
      "FROM   (SELECT COUNT(*) as num_most_frequent " +
      "        FROM SYSSTAT.COLDIST " +
      "        WHERE tabname = ? " +
      "          AND colname = ? " +
      "          AND type = 'F' " +
      "          AND colvalue is not null) as R, " +
      "       (SELECT colcard " +
      "        FROM   SYSSTAT.COLUMNS " +
      "        WHERE  tabname = ? AND colname = ?) as S";
  private final static String CONSTRAINT_QUERY =
      "SELECT type " +
      "FROM   SYSCAT.TABCONST tc, SYSCAT.KEYCOLUSE kcu " +
      "WHERE  tc.constname = kcu.constname " +
      "  AND  tc.tabname = ? AND kcu.colname = ?";
  private final static String DOMAIN_QUERY =
      "SELECT low2key, high2key, numnulls, colcard, nulls, typename, length, scale " +
      "FROM   SYSCAT.COLUMNS " +
      "WHERE  tabname = ? AND colname = ?";
  private final static String MOST_FREQUENT_QUERY =
      "SELECT colvalue, valcount " +
      "FROM   SYSSTAT.COLDIST " +
      "WHERE  tabname = ? AND colname = ? AND type = 'F' " +
      "ORDER BY seqno";
  private final static String QUANTILE_HISTOGRAM_QUERY =
      "SELECT colvalue, valcount " +
      "FROM   SYSSTAT.COLDIST " +
      "WHERE  tabname = ? AND colname = ? AND type = 'Q' " +
      "ORDER BY seqno";
  
  private final String hostname;
  private final String database;
  private final int port;
  private Connection connection;
  
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
      connection = DriverManager.getConnection(connectionString(), user, pass);

      return true;
    } catch (ClassNotFoundException e) {
      System.err.println("Could not find JDBC DB2 Driver");
      return false;
    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }
  
  public void close() throws SQLException {
    connection.close();
  }
  
  public Map<String, Class<?>> getTypeMapping() throws SQLException {
    return connection.getTypeMap();
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
  
  public TypeInfo getColumnType(String table, String column) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(TYPE_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
    if (result.next()) {
      String typeName = result.getString("typename");
      int length = result.getInt("length");
      int scale = result.getInt("scale");
      return new TypeInfo(typeName, length, scale);
    } else {
      return null;
    }
  }
  
  public boolean isNullable(String table, String column) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(DOMAIN_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
    if (result.next()) {
      boolean nullable = result.getString("nulls").charAt(0) == 'Y' ? true : false;
      return nullable;
    } else {
      return false;
    }
  }

  public Set<Constraint> getColumnConstraints(String table, String column) throws SQLException {
    Set<Constraint> constraints = Sets.newHashSet();
    PreparedStatement stmt = connection.prepareStatement(CONSTRAINT_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
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
    if (!isNullable(table, column)) {
      constraints.add(Constraint.NOT_NULL);
    }
    return constraints;
  }

  /*public ColumnDomainInfo getDomainInformation(String table, String column) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(DOMAIN_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
    if (result.next()) {
      String min = result.getString("low2key");
      String max = result.getString("high2key");
      long numNulls = result.getLong("numnulls");
      long colCard = result.getLong("colcard");
      boolean nullable = result.getString("nulls").charAt(0) == 'Y' ? true : false;
      String type = result.getString("typename");
      int length = result.getInt("LENGTH");
      int scale = result.getInt("SCALE");
      return new ColumnDomainInfo(min, max, numNulls, colCard, nullable, type, length, scale);
    } else {
      throw new RuntimeException("Could not find information about " + table + '.' + column);
    }
  }*/
  
  public String getMin(String table, String column) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(DOMAIN_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
    if (result.next()) {
      return result.getString("low2key");
    } else {
      return "";
    }
  }
  
  public Map<String, Long> getMostFrequentValues(String table, String column) throws SQLException {
    Map<String, Long> mostFrequent = Maps.newLinkedHashMap();
    PreparedStatement stmt = connection.prepareStatement(MOST_FREQUENT_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
    while (result.next()) {
      String key = result.getString("COLVALUE");
      long value = result.getLong("VALCOUNT");
      if (key != null) {
        mostFrequent.put(key, value);
      }
    }
    return mostFrequent;
  }
  
  public Map<String, Long> getQuantileHistgram(String table, String column) throws SQLException {
    Map<String, Long> qHist = Maps.newLinkedHashMap();
    PreparedStatement stmt = connection.prepareStatement(QUANTILE_HISTOGRAM_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
    while (result.next()) {
      String key = result.getString("COLVALUE");
      long value = result.getLong("VALCOUNT");
      if (key != null) {
        qHist.put(key, value);        
      }
    }
    return qHist;
  }

  public boolean isEnumerated(String table, String column) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(ENUMERATED_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    stmt.setString(3, table.toUpperCase());
    stmt.setString(4, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
    if (result.next()) {
      long colCard = result.getLong("colcard");
      int numMostFreq = result.getInt("num_most_frequent");
      return colCard <= numMostFreq;
    } else {
      return false;
    }
  }
  
  public boolean hasStatistics(String table, String column) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(DOMAIN_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    ResultSet result = stmt.executeQuery();
    if (result.next()) {
      long card = result.getLong("COLCARD");
      if (card != -1) {
        return true;
      }
    }    
    return false;
  }

}
