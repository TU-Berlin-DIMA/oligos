package de.tu_berlin.dima.oligos.type;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Types {

  private Types(){};

  public static Class<?> convert(int typeNumber) {
    Class<?> result = Object.class;

    switch(typeNumber) {
      case java.sql.Types.CHAR:
      case java.sql.Types.VARCHAR:
      case java.sql.Types.LONGVARCHAR:
        result = String.class;
        break;

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

      /*case java.sql.Types.REAL:
        result = Real
        break;*/

      case java.sql.Types.FLOAT:
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

  public static Class<?> convert(int typeNumber, int length) {
    if (typeNumber == java.sql.Types.CHAR && length == 1) {
      return Character.class;
    } else {
      return convert(typeNumber);
    }
  }

}
