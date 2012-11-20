package de.tu_berlin.dima.oligos.exception;

public class ColumnDoesNotExistException extends RuntimeException {

  private final String schema;
  private final String table;
  private final String column;

  private static final long serialVersionUID = 1L;
  
  public ColumnDoesNotExistException(final String schema, final String table
      , final String column) {
    super(schema + "." + table + "." + column);
    this.schema = schema;
    this.table = table;
    this.column = column;
  }
  
  public String getQualifiedColumnName() {
    return schema + "." + table + "." + column;
  }

}
