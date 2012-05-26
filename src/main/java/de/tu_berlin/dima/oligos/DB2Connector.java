package de.tu_berlin.dima.oligos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import de.tu_berlin.dima.oligos.histogram.QHist;

public class DB2Connector {

  private static final String JDBC_STRING = "jdbc:db2://%s:%d/%s";
  private static final String HIST_QUERY = "SELECT colvalue, valcount "
      + "FROM sysstat.coldist "
      + "WHERE tabname = ? AND colname = ? AND type = ? " + "ORDER BY seqno";
  private static final String MIN_QUERY = "SELECT MIN(<col>) FROM <table>";
  private static final String MAX_QUERY = "SELECT MAX( ? ) FROM ";

  @SuppressWarnings("serial")
  private static final Set<Class<?>> DISCRETE_TYPES = new HashSet<Class<?>>() {
    {
      add(Integer.class);
      add(Date.class);
    }
  };

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
  
  public Map<Date, Integer> getDateFHistFor(String tabName, String colName)
      throws SQLException {
    Map<Date, Integer> mostFrequent = new HashMap<Date, Integer>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    PreparedStatement stmt = conn.prepareStatement(HIST_QUERY);
    stmt.setString(1, tabName.toUpperCase());
    stmt.setString(2, colName.toUpperCase());
    stmt.setString(3, "F");
    ResultSet result = stmt.executeQuery();
    
    while (result.next()) {
      try {
        Date key = dateFormat.parse(result.getString("COLVALUE").replaceAll("\'", ""));
        Integer count = result.getInt("VALCOUNT");
        mostFrequent.put(key, count);
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    return mostFrequent;
  }

  public QHist<Date> getDateQHistFor(String tabName, String colName)
      throws SQLException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date min = new Date(0);
    ResultSet result = null;
    String query = MIN_QUERY.replaceAll("<col>", colName);
    query = query.replaceAll("<table>", tabName);
    System.out.println(query);
    PreparedStatement stmt = conn.prepareStatement(query);
    result = stmt.executeQuery();
    if (result.next()) {
      min = result.getDate(1);
    }

    stmt = conn.prepareStatement(HIST_QUERY);
    stmt.setString(1, tabName.toUpperCase());
    stmt.setString(2, colName.toUpperCase());
    stmt.setString(3, "Q");
    result = stmt.executeQuery();

    List<Date> bounds = new ArrayList<Date>();
    List<Integer> freqs = new ArrayList<Integer>();
    while (result.next()) {
      try {
        bounds.add(dateFormat.parse(result.getString("COLVALUE").replaceAll("\'", "")));
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      freqs.add(result.getInt("VALCOUNT"));
    }

    return new QHist<Date>(bounds.toArray(new Date[0]),
        ArrayUtils.toPrimitive(freqs.toArray(new Integer[0])), min);
  }

  public static boolean isDiscreteType(Class<?> clazz) {
    return DISCRETE_TYPES.contains(clazz);
  }
}
