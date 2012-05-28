package de.tu_berlin.dima.oligos.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;

import de.tu_berlin.dima.oligos.histogram.FHist;
import de.tu_berlin.dima.oligos.histogram.Operator;
import de.tu_berlin.dima.oligos.histogram.QHist;

public class DB2Connector {

  private static final String JDBC_STRING = "jdbc:db2://%s:%d/%s";
  private static final String HIST_QUERY = "SELECT colvalue, valcount "
      + "FROM sysstat.coldist "
      + "WHERE tabname = ? AND colname = ? AND type = ? " + "ORDER BY seqno";
  private static final String DOMAIN_QUERY = "SELECT low2key, high2key, numnulls, colcard "
      + "FROM sysstat.columns WHERE tabname = ? AND colname = ?";

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

  public FHist<Date> getDateFHistFor(String tabName, String colName)
      throws SQLException {
    FHist<Date> fHist = new FHist<Date>();
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      PreparedStatement stmt = conn.prepareStatement(HIST_QUERY);
      stmt.setString(1, tabName.toUpperCase());
      stmt.setString(2, colName.toUpperCase());
      stmt.setString(3, "F");
      ResultSet result = stmt.executeQuery();

      while (result.next()) {
        Date key = dateFormat.parse(result.getString("COLVALUE").replaceAll(
            "\'", ""));
        long count = result.getLong("VALCOUNT");
        fHist.addFrequentElement(key, count);

      }
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return fHist;
  }

  public QHist<Date> getDateQHistFor(String tabName, String colName)
      throws SQLException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date min = new Date(0);
    long card = 0;
    long numNulls = 0;
    ResultSet result = null;
    PreparedStatement stmt = conn.prepareStatement(DOMAIN_QUERY);
    stmt.setString(1, tabName.toUpperCase());
    stmt.setString(2, colName.toUpperCase());
    result = stmt.executeQuery();
    if (result.next()) {
      try {
        min = dateFormat.parse(result.getString("LOW2KEY").replaceAll("\'", ""));
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      card = result.getLong("COLCARD");
      numNulls = result.getLong("NUMNULLS");
    }

    stmt = conn.prepareStatement(HIST_QUERY);
    stmt.setString(1, tabName.toUpperCase());
    stmt.setString(2, colName.toUpperCase());
    stmt.setString(3, "Q");
    result = stmt.executeQuery();

    List<Date> bounds = new ArrayList<Date>();
    List<Long> freqs = new ArrayList<Long>();
    while (result.next()) {
      try {
        bounds.add(dateFormat.parse(result.getString("COLVALUE").replaceAll(
            "\'", "")));
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      freqs.add(result.getLong("VALCOUNT"));
    }
    
    Operator<Date> op = new Operator<Date>() {
      
      private Calendar cal = Calendar.getInstance();

      @Override
      public Date increment(Date value) {
        cal.setTime(value);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
      }

      @Override
      public Date decrement(Date value) {
        cal.setTime(value);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
      }
      
      @Override
      public int difference(Date val1, Date val2) {
        DateTime dt1 = new DateTime(val1);
        DateTime dt2 = new DateTime(val2);
        return Days.daysBetween(dt1, dt2).getDays();
      }
    };

    return new QHist<Date>(bounds.toArray(new Date[0]),
        ArrayUtils.toPrimitive(freqs.toArray(new Long[0])), min, card, numNulls, op);
  }

  public void close() throws SQLException {
    conn.close();
  }

  public static boolean isDiscreteType(Class<?> clazz) {
    return DISCRETE_TYPES.contains(clazz);
  }
}
