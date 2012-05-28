package de.tu_berlin.dima.oligos.type.db2;

import de.tu_berlin.dima.oligos.type.Parser;

public class IntegerParser implements Parser<Integer> {

  @Override
  public Integer parse(String input) {
    return Integer.parseInt(input);
  }

}
