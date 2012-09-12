package de.tu_berlin.dima.oligos.exception;

import com.ibm.db2.jcc.am.co;

public class ColumnDoesNotExistException extends Exception {
  
  private final String table;
  private final String column;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public ColumnDoesNotExistException(String table, String column) {
    super(table + "." + column);
    this.table = table;
    this.column = column;
  }
  
  public String getQualifiedColumnName() {
    return table + "." + column;
  }

}
