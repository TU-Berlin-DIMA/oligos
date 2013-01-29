package de.tu_berlin.dima.oligos.type.util.operator;

public class CharOperator extends AbstractOperator<Character> {

  @Override
  public Character increment(Character value) {
    return ++value;
  }

  @Override
  public Character decrement(Character value) {
    return --value;
  }

  @Override
  public long range(Character val1, Character val2) {
    return Math.abs(val2 - val1) + 1;
  }

}
