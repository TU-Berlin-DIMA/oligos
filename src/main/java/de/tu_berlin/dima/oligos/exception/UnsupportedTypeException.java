package de.tu_berlin.dima.oligos.exception;

import de.tu_berlin.dima.oligos.type.util.Constraint;

public class UnsupportedTypeException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -2605065979551646540L;

  public UnsupportedTypeException() {
    super();
  }

  public UnsupportedTypeException(final String message) {
    super(message);
  }

  public UnsupportedTypeException(
      final String type,
      final Constraint constraint) {
    super("unique " + type);
  }

}
