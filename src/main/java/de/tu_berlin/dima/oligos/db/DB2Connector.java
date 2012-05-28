package de.tu_berlin.dima.oligos.db;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import de.tu_berlin.dima.oligos.histogram.FHist;
import de.tu_berlin.dima.oligos.histogram.QHist;
import de.tu_berlin.dima.oligos.type.Operator;
import de.tu_berlin.dima.oligos.type.Parser;

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

  @SuppressWarnings("unchecked")
  public <V extends Comparable<V>> QHist<V> getQHistFor(String tabName,
      String colName, Parser<V> parser, Operator<V> op, Class<V> type)
      throws SQLException {
    V min = null;
    long card = 0;
    long numNulls = 0;
    ResultSet result = null;

    // obtain domain information
    PreparedStatement stmt = conn.prepareStatement(DOMAIN_QUERY);
    stmt.setString(1, tabName.toUpperCase());
    stmt.setString(2, colName.toUpperCase());
    result = stmt.executeQuery();
    if (result.next()) {
      min = parser.parse(result.getString("LOW2KEY"));
      card = result.getLong("COLCARD");
      numNulls = result.getLong("NUMNULLS");
    }

    // obtain quantile histogram
    stmt = conn.prepareStatement(HIST_QUERY);
    stmt.setString(1, tabName.toUpperCase());
    stmt.setString(2, colName.toUpperCase());
    stmt.setString(3, "Q");
    result = stmt.executeQuery();

    List<V> bounds = new ArrayList<V>();
    List<Long> freqs = new ArrayList<Long>();
    while (result.next()) {
      bounds.add(parser.parse(result.getString("COLVALUE")));
      freqs.add(result.getLong("VALCOUNT"));
    }
    V[] frequencies = (V[]) Array.newInstance(type, freqs.size());

    return new QHist<V>(bounds.toArray(frequencies),
        ArrayUtils.toPrimitive(freqs.toArray(new Long[0])), min, card,
        numNulls, op);
  }

  public <V extends Comparable<V>> FHist<V> getFHistFor(String table,
      String column, Parser<V> parser) throws SQLException {
    FHist<V> fHist = new FHist<V>();
    PreparedStatement stmt = conn.prepareStatement(HIST_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    stmt.setString(3, "F");
    ResultSet result = stmt.executeQuery();

    while (result.next()) {
      V key = parser.parse(result.getString("COLVALUE"));
      long count = result.getLong("VALCOUNT");
      fHist.addFrequentElement(key, count);
    }
    
    return fHist;
  }

  public void close() throws SQLException {
    conn.close();
  }

  public static boolean isDiscreteType(Class<?> clazz) {
    return DISCRETE_TYPES.contains(clazz);
  }
}
