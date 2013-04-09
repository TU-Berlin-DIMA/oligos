/*******************************************************************************
 * Copyright 2013 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tu_berlin.dima.oligos.profiler;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.Histogram;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.QuantileHistogram;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.StringHistogram;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.parser.StringParser;

public class PseudoColumnProfiler extends ColumnProfiler<String>  {

  public PseudoColumnProfiler(String schema, String table, String column,
      TypeInfo type, boolean isEnum, ColumnConnector<String> connector) {
    super(schema, table, column, type, isEnum, connector, null, new StringParser());
  }

  @Override
  public QuantileHistogram<String> getQuantileHistogram() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Column<String> profile() {
    try {
      Map<String, Long> quantileHistogram = connector.getHistogram();
      Map<String, Long> mostFrequentValues = connector.getMostFrequentValues();
      Map<String, Long> exactValues = Maps.newTreeMap();
      for (Entry<String, Long> e : mostFrequentValues.entrySet()) {
        exactValues.put(e.getKey(), e.getValue());
      }
      if (!isEnum) {
        for (Entry<String, Long> e : quantileHistogram.entrySet()) {
          String key = e.getKey();
          long value = e.getValue();
          if (!exactValues.containsKey(key)) {
            exactValues.put(key, value);
          }
        }
      }
      Set<Constraint> constraints = connector.getConstraints();      
      String min = connector.getMin();
      String max = connector.getMax();
      long cardinality = exactValues.size();
      long numNulls = connector.getNumNulls();
      Histogram<String> distribution = new StringHistogram(exactValues);
      return new Column<String>(schema, table, column, type, constraints, min, max, cardinality, numNulls, distribution, parser);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
