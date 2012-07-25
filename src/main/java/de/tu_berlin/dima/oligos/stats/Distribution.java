package de.tu_berlin.dima.oligos.stats;

public interface Distribution<T> {
  
  /**
   * Calculates the probability of the given.
   * @param value
   * @return
   *  The probability of the given value;
   */
  public double probability(T value);
  
  /**
   * Calculates the cumulative probability of all values with a given range.<br />
   * <code>lb <= X <= ub</code>
   * @param lower
   * @param upper
   * @return
   */
  public double cumulativeProbability(T lower, T upper);

}
