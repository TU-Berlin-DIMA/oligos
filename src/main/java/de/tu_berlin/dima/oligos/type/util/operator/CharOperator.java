package de.tu_berlin.dima.oligos.type.util.operator;

public class CharOperator implements Operator<Character> {

  @Override
  public int compare(Character o1, Character o2) {
    return o1.compareTo(o2);
  }

  @Override
  public Character increment(Character value) {
    return ++value;
  }

  @Override
  public Character increment(Character value, Character step) {
    return (char) (value + step);
  }

  @Override
  public Character decrement(Character value) {
    return --value;
  }

  @Override
  public Character decrement(Character value, Character step) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long difference(Character val1, Character val2) {
    return val2 - val1;
  }

  @Override
  public Character min(Character val1, Character val2) {
    if (val1 <= val2) {
      return val1;
    } else {
      return val2;
    }
  }

}
