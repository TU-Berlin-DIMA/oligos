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

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.CustomHistogram;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.Histogram;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.QuantileHistogram;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class ColumnProfiler<T> implements Profiler<Column<T>> {

  protected final ColumnConnector<T> connector;
  protected final String schema;
  protected final String table;
  protected final String column;
  protected final TypeInfo type;
  protected final boolean isEnum;
  protected final Operator<T> operator;
  protected final Parser<T> parser;

  public ColumnProfiler(final String schema, final String table, final String column
      , final TypeInfo type, final boolean isEnum, final ColumnConnector<T> connector
      , final Operator<T> operator, final Parser<T> parser) {
    this.schema = schema;
    this.table = table;
    this.column = column;
    this.type = type;
    this.isEnum = isEnum;
    this.connector = connector;
    this.operator = operator;
    this.parser = parser;
  }

  private T getMin(T low, Set<T> colvalues) {
  	T min = low;
  	for (T val : colvalues) 
  		min = operator.min(min, val);
    return min;
  }

  public QuantileHistogram<T> getQuantileHistogram() {
    try {
    	Map<T, Long> rawHist = connector.getHistogram();
    
      T min = getMin(connector.getMin(), rawHist.keySet());
      QuantileHistogram<T> histogram = new QuantileHistogram<T>(min, operator);
      for (Entry<T, Long> entry : rawHist.entrySet()) {
        T value = entry.getKey();
        long count = entry.getValue();
        histogram.addBound(value, count);
      }
      return histogram;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Column<T> profile() {
  	try {
    	Set<Constraint> constraints = connector.getConstraints();
      T min = connector.getMin();
    	T max = connector.getMax();
    	long cardinality = connector.getCardinality();
  		long numNulls = connector.getNumNulls();
  		Histogram<T> distribution = null;
  		if (isEnum) {
  			distribution = new CustomHistogram<T>(operator);
  			for (Entry<T, Long> e : connector.getMostFrequentValues().entrySet()) {
  				distribution.add(e.getKey(), e.getKey(), e.getValue());
  			}
  		} else {
  			distribution = getQuantileHistogram();
  		}
  		return new Column<T>(schema, table, column, type, constraints, min, max,
          cardinality, numNulls, distribution, parser);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
