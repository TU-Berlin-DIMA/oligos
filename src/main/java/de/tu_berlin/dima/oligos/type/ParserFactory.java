package de.tu_berlin.dima.oligos.type;

import java.math.BigDecimal;
import java.sql.Date;

import de.tu_berlin.dima.oligos.type.db2.DateParser;
import de.tu_berlin.dima.oligos.type.db2.DecimalParser;
import de.tu_berlin.dima.oligos.type.db2.DoubleParser;
import de.tu_berlin.dima.oligos.type.db2.IntegerParser;

public class ParserFactory {
  
  private static String DATE_FORMAT = "yyyy-MM-dd";
  private static int DECIMAL_SCALE = 2;
  
  public static void setDateFormat(String dataFormat) {
    DATE_FORMAT = dataFormat;
  }
  
  public static void setDecimalFormat(int scale) {
    DECIMAL_SCALE = scale;
  }

  public static Parser<?> createParser(Class<?> type) {
    Parser<?> parser = null;
    
    if (type.equals(Short.class)) {
      throw new UnsupportedOperationException("Not yet supported");
    }
    
    else if (type.equals(Integer.class)) {
      parser = new IntegerParser();
    }
    
    else if (type.equals(Long.class)) {
      throw new UnsupportedOperationException("Not yet supported");
    }
    
    else if (type.equals(Float.class)) {
      throw new UnsupportedOperationException("Not yet supported");
    }
    
    else if (type.equals(Double.class)) {
      parser = new DoubleParser();
    }
    
    else if (type.equals(Date.class)) {
      parser = new DateParser(DATE_FORMAT, DATE_FORMAT);
    }
    
    else if (type.equals(BigDecimal.class)) {
      parser = new DecimalParser(DECIMAL_SCALE);
    }
    
    else if (type.equals(String.class)) {
      throw new UnsupportedOperationException("Not yet supported");
    }
    
    return parser;
  }
}
