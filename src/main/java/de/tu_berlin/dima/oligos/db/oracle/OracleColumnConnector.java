/*******************************************************************************
 * Copyright 2014 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.tu_berlin.dima.oligos.db.oracle;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

/**
 * Oracle specific {@link de.tu_berlin.dima.oligos.db.ColumnConnector} provides a mapping between catalog meta data
 * (not available through JDBC) and Java types.
 */
public final class OracleColumnConnector<T> implements ColumnConnector<T> {

  private static final Logger LOGGER = Logger.getLogger(OracleColumnConnector.class);
  private static final long UNINITIALIZED = -1L;

  private static final String DOMAIN_QUERY =
          "SELECT low_value, high_value, num_nulls, num_distinct, nullable, data_type " +
          "FROM ALL_TAB_COLUMNS " +
          "WHERE owner = ? AND table_name = ? AND column_name = ?";

  private static final String CONSTRAINT_QUERY =
          "SELECT constraint_type as type FROM ALL_CONSTRAINTS ac, ALL_CONS_COLUMNS cc " +
          "WHERE ac.constraint_name = cc.constraint_name " +
            "AND ac.owner = ? AND ac.table_name = ? " +
            "AND cc.column_name = ?";

  private static final String HISTOGRAM_TYPE =
          "SELECT HISTOGRAM " +
          "FROM ALL_TAB_COL_STATISTICS " +
          "WHERE OWNER = ? " +
            "AND TABLE_NAME = ? " +
            "AND COLUMN_NAME = ?";

  private static final String HISTOGRAM_QUERY =
          "SELECT endpoint_value, endpoint_actual_value, endpoint_number, endpoint_repeat_count " +
          "FROM all_tab_histograms " +
          "WHERE owner = ? " +
            "AND table_name = ? " +
            "AND column_name = ?" +
          "ORDER BY endpoint_number";

  private final JdbcConnector connector;
  private final String schema;
  private final String table;
  private final String column;
  private final Parser<T> parser;

  /* CACHED VALUES, field are initialized in a lazy manner */
  // domain query cache
  private boolean isDomainCached = false;
  private long numNulls = UNINITIALIZED;
  private long cardinality = UNINITIALIZED;
  private T min = null;
  private T max = null;
  private TypeInfo typeInfo = null;
  // constraint cache
  private Set<Constraint> constraints;
  // histogram cache
  private HistogramType histogramType = null;
  private Map<T, Pair<Long, Long>> rawHistogram = null;
  private Map<T, Long> frequencyHistogram = null;
  private Map<T, Long> histogram = null;

  public OracleColumnConnector(
          final JdbcConnector connector,
          final String schema, final String table, final String column,
          final Parser<T> parser) {
    this.connector = connector;
    this.schema = schema;
    this.table = table;
    this.column = column;
    this.parser = parser;
  }

  @SuppressWarnings({"unchecked"})
  private void getDomainValues() throws SQLException {
    if (!isDomainCached) {
      Map<String, Object> result = connector.mapQuery(DOMAIN_QUERY, schema, table, column);
      isDomainCached = true;
      numNulls = ((BigDecimal) result.get("NUM_NULLS")).longValueExact();
      cardinality = ((BigDecimal) result.get("NUM_DISTINCT")).longValueExact();
      min = (T) OracleUtils.convert((byte[]) result.get("LOW_VALUE"), getTypeInfo());
      max = (T) OracleUtils.convert((byte[]) result.get("HIGH_VALUE"), getTypeInfo());
    }
  }

  private HistogramType getHistogramType() throws SQLException {
    if (histogramType == null) {
      String type = connector.scalarQuery(HISTOGRAM_TYPE, "HISTOGRAM", schema, table, column);
      // map histogram string to java enum (e.g., HEIGHT BALANCED -> HEIGHT_BALANCED)
      histogramType = HistogramType.valueOf(type.replace(' ', '_'));
    }
    return histogramType;
  }

  private TypeInfo getTypeInfo() throws SQLException {
    if (typeInfo == null) {
      typeInfo = connector.typeQuery(schema, table, column);
    }
    return typeInfo;
  }

  @Override
  public long getNumNulls() throws SQLException {
    if (!isDomainCached) {
      getDomainValues();
    }
    return numNulls;
  }

  @Override
  public long getCardinality() throws SQLException {
    if (!isDomainCached) {
      getDomainValues();
    }
    return cardinality;
  }

  @Override
  public Set<Constraint> getConstraints() throws SQLException {
    if (constraints == null) {
      constraints = Sets.newHashSet();
      ResultSetHandler<List<String>> handler = new ColumnListHandler<>("TYPE");
      QueryRunner runner = new QueryRunner(true);
      List<String> cons = runner.query(connector.getConnection(), CONSTRAINT_QUERY, handler, schema, table, column);

      if (cons.contains("U")) {
        constraints.add(Constraint.UNIQUE);
      }
      else if (cons.contains("P")) {
        constraints.add(Constraint.PRIMARY_KEY);
      }
      else if (cons.contains("R")) {
        constraints.add(Constraint.FOREIGN_KEY);
      }

    }
    return constraints;
  }

  @Override
  public T getMin() throws SQLException {
    if (!isDomainCached) {
      getDomainValues();
    }
    return min;
  }

  @Override
  public T getMax() throws SQLException {
    if (!isDomainCached) {
      getDomainValues();
    }
    return max;
  }

  /**
   * Retrieves the raw histogram from the Oracle Database catalog regardless of its type (i.e. frequency, height
   * balanced, or hybrid histogram). Cumulative frequencies are converted to absolute frequencies.
   * <br />
   * For example
   * <pre>
   *   value 1  1
   *   value 2  2
   *   value 3  3
   *   value 4  4
   *   ...
   * </pre>
   * is converted to
   * <pre>
   *   value 1  1
   *   value 2  1
   *   value 3  1
   *   value 4  1
   *   ...
   * </pre>.
   * <br />
   * The entries are ordered by there respective endpoint_number (before conversion).
   * @return Mapping of values (bucket boundaries) to there apsolute count and repeat count if present.
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  public Map<T, Pair<Long, Long>> getRawHistogram() throws SQLException {
    if (rawHistogram == null) {
      rawHistogram = new LinkedHashMap<>();
      Connection conn = connector.getConnection();
      PreparedStatement stmt = conn.prepareStatement(HISTOGRAM_QUERY);
      stmt.setString(1, schema);
      stmt.setString(2, table);
      stmt.setString(3, column);
      ResultSet result = stmt.executeQuery();
      long last = 0L;
      while (result.next()) {
        BigDecimal endpointValue = result.getBigDecimal("ENDPOINT_VALUE");
        String endpointActualValue = result.getString("ENDPOINT_ACTUAL_VALUE");
        long cumulativeCount = result.getLong("ENDPOINT_NUMBER");
        long endpointRepeatCount = result.getLong("ENDPOINT_REPEAT_COUNT");
        long count = cumulativeCount - last;
        if (getTypeInfo().getType().equals(String.class)
                && endpointActualValue != null
                && endpointActualValue.length() > 0) {
          rawHistogram.put(parser.fromString(endpointActualValue), Pair.of(count, endpointRepeatCount));
        } else {
          rawHistogram.put((T) OracleUtils.convertFromNumber(endpointValue, getTypeInfo()), Pair.of(count, endpointRepeatCount));
        }
        last = cumulativeCount;
      }
      stmt.close();
    }
    return rawHistogram;
  }

  @Override
  public Map<T, Long> getMostFrequentValues() throws SQLException {
    // TODO what about HYBRID histograms, is it possible to extract a frequency histogram?
    if (frequencyHistogram == null) {
      frequencyHistogram = new LinkedHashMap<>();
      HistogramType histType = getHistogramType();
      Map<T, Pair<Long, Long>> rawHist = getRawHistogram();
      if (histType == HistogramType.FREQUENCY) {
        // drop the repeat count part (right component of pair) from the raw histogram
        for (Map.Entry<T, Pair<Long, Long>> e : rawHist.entrySet()) {
          T value = e.getKey();
          Long count = e.getValue().getLeft();
          frequencyHistogram.put(value, count);
        }
      } else if (histType == HistogramType.HYBRID) {
        // extract the exact counts (i.e. endpoints)
        for (Map.Entry<T, Pair<Long, Long>> e : rawHist.entrySet()) {
          T value = e.getKey();
          //Long count = e.getValue().getLeft();
          Long repeat = e.getValue().getRight();
          if (repeat > 0) {
            frequencyHistogram.put(value, repeat);
          }
        }
      }
    }
    return frequencyHistogram;
  }

  @Override
  public Map<T, Long> getHistogram() throws SQLException {
    if (histogram == null) {
      histogram = new LinkedHashMap<>();
      HistogramType histType = getHistogramType();
      Map<T, Pair<Long, Long>> rawHist = getRawHistogram();
      if (histType == HistogramType.HEIGHT_BALANCED || histType == HistogramType.HYBRID) {
        for (Map.Entry<T, Pair<Long, Long>> e : rawHist.entrySet()) {
          T value = e.getKey();
          Long count = e.getValue().getLeft();
          histogram.put(value, count);
        }
      }
    }
    return histogram;
  }

  private static enum HistogramType {
    NONE,
    FREQUENCY,
    //TOP_FREQUENCY,
    HYBRID,
    HEIGHT_BALANCED
  }
}
