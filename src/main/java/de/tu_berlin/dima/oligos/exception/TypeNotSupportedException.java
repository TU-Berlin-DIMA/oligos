package de.tu_berlin.dima.oligos.exception;

public class TypeNotSupportedException extends Exception {

  private static final long serialVersionUID = 1L;
  
  private final String type;
  
  public TypeNotSupportedException() {
    super();
    this.type = "";
  }
  
  public TypeNotSupportedException(String type) {
    super();
    this.type = type;
  }
  
  @Override
  public String getMessage() {
    return type;
  }

}
