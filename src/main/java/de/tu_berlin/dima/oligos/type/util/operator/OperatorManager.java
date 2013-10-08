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
package de.tu_berlin.dima.oligos.type.util.operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.type.util.operator.date.DateOperator;
import de.tu_berlin.dima.oligos.type.util.operator.date.TimeOperator;
import de.tu_berlin.dima.oligos.type.util.operator.date.TimestampOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.BigDecimalOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.BigIntegerOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.ByteOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.DoubleOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.FloatOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.IntegerOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.LongOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.ShortOperator;

public final class OperatorManager {

  private final static Map<Class<?>, Operator<?>> operators = Maps.newHashMap();

  private OperatorManager(){}
  
  public static <T> void putOperator(Class<T> clazz, Operator<T> operator) {
    operators.put(clazz, operator);
  }

  public static <T> boolean hasOperator(Class<T> clazz) {
    return (operators.get(clazz) != null) ? true : false;
  }
 
  @SuppressWarnings("unchecked")
  public static <T> Operator<T> getOperator(Class<T> clazz) {
    Operator<T> operator = (Operator<T>) operators.get(clazz);
    if (operator == null) {
      throw new RuntimeException("No operator found for " + clazz.getSimpleName());
    }
    return operator;
  }

  public static Collection<Class<?>> getSupportedTypes() {
    return operators.keySet();
  }

  // initialize default operators
  static{
    putOperator(Byte.class, new ByteOperator());
    putOperator(Short.class, new ShortOperator());
    putOperator(Integer.class, new IntegerOperator());
    putOperator(Long.class, new LongOperator());
    putOperator(Timestamp.class, new TimestampOperator());
    putOperator(Time.class, new TimeOperator());
    putOperator(Date.class, new DateOperator());
    putOperator(BigInteger.class, new BigIntegerOperator());
    putOperator(BigDecimal.class, new BigDecimalOperator());
    putOperator(Character.class, new CharOperator());
    putOperator(Double.class, new DoubleOperator());
    putOperator(Float.class, new FloatOperator());
    putOperator(String.class, new StringOperator());
  }

}
