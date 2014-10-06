/*******************************************************************************
 * Copyright 2013 - 2014 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
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
package de.tu_berlin.dima.oligos.stat;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import de.tu_berlin.dima.oligos.stat.distribution.histogram.Histogram;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class Column<T> {

  @SuppressWarnings("serial")
  public static final Set<Class<?>> INCREMENTAL_TYPES = new HashSet<Class<?>>() {{
    add(Character.class);
    add(Short.class);
    add(Integer.class);
    add(Long.class);
    add(Time.class);
    add(Timestamp.class);
    add(Date.class);
    add(BigDecimal.class);
    add(Float.class);
    add(Double.class);
  }};

  private final String schema;
  private final String table;
  private final String column;
  private final TypeInfo type;
  private final Set<Constraint> constraints;
  private final T min;
  private final T max;
  private final long cardinality;
  private final long numNulls;
  private final Histogram<T> distribution;
  private final Parser<T> parser;

  public Column(final String schema, final String table, final String column
      , final TypeInfo type, final Set<Constraint> constraints
      , final T min, final T max
      , final long cardinality, final long numNulls
      , final Histogram<T> distribution, final Parser<T> parser) {
    this.schema = schema;
    this.table = table;
    this.column = column;
    this.type = type;
    this.constraints = constraints;
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.numNulls = numNulls;
    this.distribution = distribution;
    this.parser = parser;
  }

  public ColumnId getId() {
    return new ColumnId(schema, table, column);
  }
  
  public String getSchema() {
    return schema;
  }
  
  public String getTable() {
    return table;
  }
  
  public String getColumn() {
    return column;
  }

  public String getQualifiedName() {
    return schema + "." + table + "." + column;
  }

  public TypeInfo getTypeInfo() {
    return type;
  }

  public String getTypeName() {
    return type.getTypeName();
  }

  public Set<Constraint> getConstraints() {
    return constraints;
  }

  public T getMin() {
    return min;
  }

  public T getMax() {
    return max;
  }

  public long getCardinality() {
    return cardinality;
  }

  public long getNumNulls() {
    return numNulls;
  }
  
  public boolean isEnumerated() {
    if (INCREMENTAL_TYPES.contains(type.getType())) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Checks whether the column has a constraint that implies uniqueness.
   * <br />
   * For instance primary key columns or columns with the <code>UNIQUE</code> constraint.
   * @return True if column is enforced to be unique, false otherwise.
   */
  public boolean isUniqueHard() {
    if (constraints.contains(Constraint.PRIMARY_KEY) || constraints.contains(Constraint.UNIQUE)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isUniqueSoft() {
    return distribution.getTotalNumberOfValues() <= distribution.getCardinality();
  }
  
  public boolean hasDistribution() {
    return !distribution.isEmpty();
  }

  public long getNumberOfRecords() {
    return distribution.getTotalNumberOfValues() + numNulls;
  }
  
  public long getNumberOfValues() {
    return distribution.getTotalNumberOfValues();
  }
  
  public Histogram<T> getDistribution() {
    return distribution;
  }

  public double getNullProbability() {
    if (numNulls == 0) {
      return 0.0;
    } else {
      return (double) numNulls / getNumberOfRecords(); 
    }
  }

  public String asString(Object value) {
    return parser.toString(value);
  }

  @Override
  public String toString() {
    return getQualifiedName();
  }
}
