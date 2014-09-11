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

package de.tu_berlin.dima.oligos.db.oracle;

import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import oracle.sql.CharacterSet;
import oracle.sql.DATE;
import oracle.sql.NUMBER;
import oracle.sql.TIMESTAMP;
import org.joda.time.DateTimeUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Utility class for methods specific to Oracle Database.
 * <br/>
 * This class contains static methods to convert different values of different
 * types to other types.
 */
public final class OracleUtils {

  /**
   * Utility class should not be instantiated.
   */
  private OracleUtils() {}

  /**
   * Converts a number representation of a String to a String.
   * @param number Decimal number representing a character string
   * @param type Type information for the String column, such as length
   * @param withPadding If true all non-reconstructable characters are replaced
   *                    by whitespace until String length is reached.
   * @return {@link java.lang.String}
   */
  public static String numberToString(BigDecimal number, TypeInfo type, boolean withPadding) {
    char s[] = new char[type.getLength()];
    int nl;
    BigDecimal divisor;
    BigDecimal C256 = BigDecimal.valueOf(256);
    int exactDigits = 6;
    // compute all reconstructable characters
    for (int i = 1; i <= Math.min(exactDigits, type.getLength()); i++){
      divisor = C256.pow(15-i);
      nl = number.divide(divisor).intValue();
      if (nl == 0){
        exactDigits = i-1;
        break;
      }
      number = number.subtract((new BigDecimal(nl)).multiply(divisor));
      s[i-1] = (char) nl;
    }
    // pad rest with blanks
    if (withPadding) {
      for (int i = exactDigits; i < type.getLength(); i++)
        s[i] = (char) 32;
    }
    return new String(s);
  }

  public static Object convertFromNumber(BigDecimal number, TypeInfo type) throws SQLException {
    Object result;

    switch (type.getTypeName().toLowerCase()) {
      case "date":
        result = new Date(DateTimeUtils.fromJulianDay(number.doubleValue()));
        break;
      case "time":
        result = new Time(DateTimeUtils.fromJulianDay(number.doubleValue()));
        break;
      case "timestamp":
        result = new Timestamp(DateTimeUtils.fromJulianDay(number.doubleValue()));
        break;
      case "number":
        if (type.getScale() == 0) {
          result = number.toBigIntegerExact();
        } else {
          result = number;
        }
        break;
      case "char":
      case "varchar":
      case "varchar2":
        String temp = OracleUtils.numberToString(number, type, true);
        result = (type.getLength() == 1) ? temp.charAt(0) : temp.trim();
        break;
      default:
        throw new RuntimeException("Unable to convert from number: Unsupported type " + type.getTypeName());
    }

    return result;
  }

  public static Object convert(byte[] raw, TypeInfo type) throws SQLException {
    Object result;

    // TODO add support for floating point numbers
    switch (type.getTypeName().toLowerCase()) {
      case "date":
        result = DATE.toDate(raw);
        break;
      case "timestamp":
        result = TIMESTAMP.toTimestamp(raw);
        break;
      case "time":
        result = TIMESTAMP.toTime(raw);
        break;
      case "number":
        if (type.getScale() == 0) {
          result = NUMBER.toBigInteger(raw);
        } else {
          result = NUMBER.toBigDecimal(raw);
        }
        break;
      case "char":
      case "varchar":
      case "varchar2":
        String temp = CharacterSet.UTFToString(raw, 0, raw.length);
        result = (type.getLength() == 1) ? temp.charAt(0) : temp;
        break;
      default:
        throw new RuntimeException("Unable to convert from raw: Unknown type " + type.getTypeName());
    }
    return result;
  }
}
