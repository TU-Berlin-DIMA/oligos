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
package de.tu_berlin.dima.oligos.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class HistogramHandler<T> implements ResultSetHandler<Map<T, Long>> {

  private final int keyColumnIndex;
  private final String keyColumnName;
  private final int valColumnIndex;
  private final String valColumnName;
  private final Parser<T> parser;

  public HistogramHandler(
      final String keyColumnName,
      final String valueColumnName,
      final Parser<T> parser) {
    this.keyColumnName = keyColumnName;
    this.valColumnName = valueColumnName;
    this.parser = parser;
    this.keyColumnIndex = Integer.MIN_VALUE;
    this.valColumnIndex = Integer.MIN_VALUE;
  }

  public HistogramHandler(
      final int keyColumnIndex,
      final int valueColumnIndex,
      final Parser<T> parser) {
    this.keyColumnName = null;
    this.valColumnName = null;
    this.parser = parser;
    this.keyColumnIndex = keyColumnIndex;
    this.valColumnIndex = valueColumnIndex;
  }

  @Override
  public Map<T, Long> handle(ResultSet rs) throws SQLException {
    Map<T, Long> mostFrequentValues = Maps.newLinkedHashMap();
    while (rs.next()) {
      String colvalue = (keyColumnName != null) ? rs.getString(keyColumnName) : rs.getString(keyColumnIndex) ;
      if (colvalue != null) {
        T value = parser.fromString(colvalue.replaceAll("'", ""));
        long count = (valColumnName != null) ? rs.getLong(valColumnName) : rs.getLong(valColumnIndex);
        mostFrequentValues.put(value, count);
      }
    }
    return mostFrequentValues;
  }


}
