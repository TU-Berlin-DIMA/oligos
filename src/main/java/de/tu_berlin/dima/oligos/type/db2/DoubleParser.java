package de.tu_berlin.dima.oligos.type.db2;

import de.tu_berlin.dima.oligos.type.Parser;

public class DoubleParser implements Parser<Double> {

  @Override
  public Double parse(String input) {
    return Double.parseDouble(input);
  }
  
  @Override
  public String format(Double input) {
    return input.toString();
  }

}
