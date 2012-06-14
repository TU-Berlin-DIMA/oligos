package de.tu_berlin.dima.oligos.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;

import de.tu_berlin.dima.oligos.histogram.BucketHistogram;
import de.tu_berlin.dima.oligos.histogram.CombinedHist;
import de.tu_berlin.dima.oligos.histogram.ElementHistogram;
import de.tu_berlin.dima.oligos.type.Type;
import de.tu_berlin.dima.oligos.type.AbstractTypeFactory;
import de.tu_berlin.dima.oligos.type.db2.DB2TypeFactory;

public class DB2Connector {

  private static final String JDBC_STRING = "jdbc:db2://%s:%d/%s";
  private static final String HIST_QUERY = "SELECT colvalue, valcount "
      + "FROM sysstat.coldist "
      + "WHERE tabname = ? AND colname = ? AND type = ? " + "ORDER BY seqno";
  private static final String DOMAIN_QUERY = "SELECT low2key, high2key, numnulls, colcard "
      + "FROM sysstat.columns WHERE tabname = ? AND colname = ?";
  private static final String TYPE_QUERY = "SELECT typename FROM syscat.columns WHERE tabname = ? AND colname = ?";

  @SuppressWarnings("serial")
  private static final Map<String, Class<?>> TYPE_MAPPING = new HashMap<String, Class<?>>() {
    {
      put("SMALLINT", Short.class);
      put("INTEGER", Integer.class);
      put("BIGINT", Long.class);
      put("REAL", Float.class);
      put("DOUBLE", Double.class);
      put("DATE", Date.class);
      put("DECIMAL", BigDecimal.class);
      put("CHARACTER", String.class);
      put("VARCHAR", String.class);
    }
  };

  @SuppressWarnings("serial")
  private static final Set<Class<?>> PARAM_TYPES = new HashSet<Class<?>>() {
    {
      add(BigDecimal.class);
    }
  };

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

  private final AbstractTypeFactory typeFactory;

  public DB2Connector(final String host, final String db, final int port) {
    this.hostname = host;
    this.database = db;
    this.port = port;
    this.typeFactory = new DB2TypeFactory();
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

  public Class<?> getColumnType(String tableName, String columnName)
      throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(TYPE_QUERY);
    stmt.setString(1, tableName.toUpperCase());
    stmt.setString(2, columnName.toUpperCase());
    ResultSet result = stmt.executeQuery();
    if (result.next()) {
      String typeString = result.getString("TYPENAME");
      return TYPE_MAPPING.get(typeString);
    } else {
      throw new IllegalArgumentException();
    }
  }

  public BucketHistogram<?> getBucketHistogram(String table, String column,
      Class<?> type) throws SQLException {
    Type<?> min = null;
    long card = 0l;
    long numNulls = 0l;
    ResultSet result = null;

    // obtain domain information
    PreparedStatement stmt = conn.prepareStatement(DOMAIN_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    result = stmt.executeQuery();
    if (result.next()) {
      min = typeFactory.createType(type, result.getString("LOW2KEY"));
      card = result.getLong("COLCARD");
      numNulls = result.getLong("NUMNULLS");
    }

    // obtain quantile histogram
    stmt = conn.prepareStatement(HIST_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    stmt.setString(3, "Q");
    result = stmt.executeQuery();

    List<Type<?>> bounds = Lists.newArrayList();
    List<Long> freqs = new ArrayList<Long>();
    while (result.next()) {
      Type<?> b = typeFactory.createType(type, result.getString("COLVALUE"));
      bounds.add(b);
      freqs.add(result.getLong("VALCOUNT"));
    }
    // Type<?>[] frequencies = (Type<?>[]) Array.newInstance(min.getClass(),
    // freqs.size());
    Type<?>[] boundaries = bounds.toArray(new Type<?>[0]);
    long[] frequencies = ArrayUtils.toPrimitive(freqs.toArray(new Long[0]));
    // return new QHist<Type<?>>(bounds.toArray(frequencies),
    // ArrayUtils.toPrimitive(freqs.toArray(new Long[0])), min, card, numNulls);
    return typeFactory.createBucketHistogram(boundaries, frequencies, min,
        card, numNulls);
  }

  public ElementHistogram<?> getElementHistogram(String table, String column,
      Class<?> type) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(HIST_QUERY);
    stmt.setString(1, table.toUpperCase());
    stmt.setString(2, column.toUpperCase());
    stmt.setString(3, "F");
    ResultSet result = stmt.executeQuery();

    List<Type<?>> elems = Lists.newArrayList();
    List<Long> freqs = Lists.newArrayList();

    while (result.next()) {
      Type<?> key = typeFactory.createType(type, result.getString("COLVALUE"));
      long count = result.getLong("VALCOUNT");
      freqs.add(count);
      elems.add(key);
    }

    return typeFactory.createElementHistogram(elems.toArray(new Type<?>[0]),
        ArrayUtils.toPrimitive(freqs.toArray(new Long[0])));
  }

  public CombinedHist<?> profileColumn(String tableName, String columnName)
      throws SQLException {
    Class<?> type = getColumnType(tableName, columnName);
    BucketHistogram<?> bHist = getBucketHistogram(tableName, columnName, type);
    ElementHistogram<?> eHist = getElementHistogram(tableName, columnName, type);
    return typeFactory.createCombinedHistogram(eHist, bHist);
  }

  public void close() throws SQLException {
    conn.close();
  }

  public static boolean isDiscreteType(Class<?> clazz) {
    return DISCRETE_TYPES.contains(clazz);
  }

  public static boolean hasParameters(Class<?> clazz) {
    return PARAM_TYPES.contains(clazz);
  }
}
