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

/**
 * This class is a container of string constants used as column names in JDBC
 * ResultSets. This classed could be staticaly imported to ease the use with
 * ResultSets coming from the JDBC driver.
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 */
public abstract class JdbcConstants {

  /**
   * This class contains string constants used for identifiers. E.g., schema
   * names, table names, or column names.
   * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
   *
   */
  public static class Identifiers {
    public final static String SCHEMA_NAME = "TABLE_SCHEM";
    public final static String TABLE_NAME = "TABLE_NAME";
    public final static String COLUMN_NAME = "COLUMN_NAME";
  }

  /**
   * This class contains key related string constants.
   * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
   *
   */
  public static class Keys {
    public final static String PARENT_SCHEMA = "PKTABLE_SCHEM";
    public final static String PARENT_TABLE = "PKTABLE_NAME";
    public final static String PARENT_COLUMN = "PKCOLUMN_NAME";
    public final static String CHILD_SCHEMA = "FKTABLE_SCHEM";
    public final static String CHILD_TABLE = "FKTABLE_NAME";
    public final static String CHILD_COLUMN = "FKCOLUMN_NAME";
    public final static String SEQUENCE_NUMBER = "KEY_SEQ";
  }

  /**
   * This class contains column related string constants. They could be used to
   * obtain meta data about columns.
   * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
   *
   */
  public static class Columns {
    public final static String DATA_TYPE = "DATA_TYPE";
    public final static String TYPE_NAME = "TYPE_NAME";
    public final static String LENGTH = "COLUMN_SIZE";
    public final static String SCALE = "DECIMAL_DIGITS";
  }

}
