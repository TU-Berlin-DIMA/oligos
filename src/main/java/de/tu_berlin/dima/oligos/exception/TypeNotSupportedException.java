package de.tu_berlin.dima.oligos.exception;

public class TypeNotSupportedException extends Exception {

  private static final long serialVersionUID = 1L;
  
  private final String type;
  private final int length;
  
  public TypeNotSupportedException() {
    super();
    this.type = "";
    this.length = 0;
  }
  
  public TypeNotSupportedException(String type) {
    super();
    this.type = type;
    this.length = 0;
  }
  
  public TypeNotSupportedException(String type, int length) {
    super();
    this.type = type;
    this.length = length;
  }
  
  @Override
  public String getMessage() {
    return type + "(" + length + ")";
  }

}
