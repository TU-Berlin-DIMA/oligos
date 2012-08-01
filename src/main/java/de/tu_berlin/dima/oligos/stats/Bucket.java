package de.tu_berlin.dima.oligos.stats;

public class Bucket<T> {
  private final T lowerBound;
  private final T upperBound;
  private final long frequency;
  
  public Bucket(final T lowerBound, final T upperBound, final long frequency) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.frequency = frequency;
  }
  
  public T getLowerBound() {
    return lowerBound;
  }
  
  public T getUpperBound() {
    return upperBound;
  }
  
  public long getFrequency() {
    return frequency;
  }
  
  @Override
  public String toString() {
    return "" + lowerBound + '\t' + upperBound + '\t' + frequency;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (frequency ^ (frequency >>> 32));
    result = prime * result
        + ((lowerBound == null) ? 0 : lowerBound.hashCode());
    result = prime * result
        + ((upperBound == null) ? 0 : upperBound.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("unchecked")
    Bucket<T> other = (Bucket<T>) obj;
    if (frequency != other.frequency)
      return false;
    if (lowerBound == null) {
      if (other.lowerBound != null)
        return false;
    } else if (!lowerBound.equals(other.lowerBound))
      return false;
    if (upperBound == null) {
      if (other.upperBound != null)
        return false;
    } else if (!upperBound.equals(other.upperBound))
      return false;
    return true;
  }  
  
}
