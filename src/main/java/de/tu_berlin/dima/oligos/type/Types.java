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
package de.tu_berlin.dima.oligos.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;

import com.google.common.collect.Maps;

public class Types {

  private final static Map<Class<?>, MyriadType> MYRIAD_TYPES = Maps.newHashMap();

  private Types(){};
  
  /* convert SQL data type into a convenient Java class */
  public static Class<?> convert(int typeNumber) {
    Class<?> result = Object.class;

    switch(typeNumber) {
      case java.sql.Types.CHAR:
      case java.sql.Types.VARCHAR:
      case java.sql.Types.LONGVARCHAR:
        result = String.class;
        break;
      
      
     /* case oracle.jdbc.OracleTypes.NUMBER:
        result = BigDecimal.class;
        break;*/
        
      case java.sql.Types.NUMERIC:
      case java.sql.Types.DECIMAL:
        result = BigDecimal.class;
        break;

      case java.sql.Types.BIT:
        result = Boolean.class;
        break;

      case java.sql.Types.TINYINT:
        result = Byte.class;
        break;

      case java.sql.Types.SMALLINT:
        result = Short.class;
        break;

      case java.sql.Types.INTEGER:
        result = Integer.class;
        break;

      case java.sql.Types.BIGINT:
        result = Long.class;
        break;

      case java.sql.Types.FLOAT:
      case java.sql.Types.REAL:
        result = Float.class;

      case java.sql.Types.DOUBLE:
        result = Double.class;
        break;

      case java.sql.Types.BINARY:
      case java.sql.Types.VARBINARY:
      case java.sql.Types.LONGVARBINARY:
        result = Byte[].class;
        break;

      case java.sql.Types.DATE:
        result = Date.class;
        break;

      case java.sql.Types.TIME:
        result = Time.class;
        break;

      case java.sql.Types.TIMESTAMP:
        result = Timestamp.class;
        break;
    }

    return result;
  }

  /* convert CHAR(1) or VARCHAR(1) into Java's Character class */
  public static Class<?> convert(int typeNumber, int length) {
    if ((typeNumber == java.sql.Types.CHAR || typeNumber == java.sql.Types.VARCHAR) 
        && length == 1) {
      return Character.class;
    } else {
      return convert(typeNumber);
    }
  }

  public static MyriadType getMyriadType(Class<?> clazz) {
    MyriadType myriadType = MYRIAD_TYPES.get(clazz);
    if (myriadType != null) {
      return myriadType;
    } else {
      throw new RuntimeException("Type " + clazz.getSimpleName() + "is not supported");
    }
  }

  /**
   * Initialize type mappings
   */
  static{
    initMyriadTypeMapping(MYRIAD_TYPES);
  }

  private static void initMyriadTypeMapping(Map<Class<?>, MyriadType> typeMappings) {
    // integral types
    //typeMappings.put(Byte.class, "");
    typeMappings.put(Short.class, MyriadType.I16);
    typeMappings.put(Integer.class, MyriadType.I32);
    typeMappings.put(Long.class, MyriadType.I64);
    typeMappings.put(BigInteger.class, MyriadType.I64);
    // real types
    typeMappings.put(Float.class, MyriadType.Decimal);
    typeMappings.put(Double.class, MyriadType.Decimal);
    typeMappings.put(BigDecimal.class, MyriadType.Decimal);
    // textual types
    typeMappings.put(Character.class, MyriadType.Char);
    typeMappings.put(String.class, MyriadType.String);
    // time types
    typeMappings.put(Timestamp.class, MyriadType.Date);
    typeMappings.put(Time.class, MyriadType.Date);
    typeMappings.put(Date.class, MyriadType.Date);
  }

}
