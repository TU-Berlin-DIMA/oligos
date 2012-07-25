package de.tu_berlin.dima.oligos.stats;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.tu_berlin.dima.oligos.type.util.Operator;

public class QuantileHistogram<T> implements Histogram<T> {
  
  private final T min;
  private final T[] boundaries; // upper bounds (a <= ub)
  private final long frequencies[];
  private final Operator<T> operator;

  public QuantileHistogram(T min, T[] boundaries, long[] frequencies, Operator<T> operator) {
    Preconditions.checkArgument(boundaries.length == frequencies.length);
    this.boundaries = boundaries;
    this.frequencies = frequencies;
    this.min = min;
    this.operator = operator;
  }

  @Override
  public int getNumberOfBuckets() {
    return boundaries.length;
  }

  @Override
  public int getBucketOf(T value) {
    int index = 0;
    int len = getNumberOfBuckets();
    while (operator.compare(boundaries[index], value) < 0) {
      if (index < len - 1) {
        index++;
      } else {
        index = -1;
        break;
      }
    }
    return index;
  }

  @Override
  public long getTotalNumberOfValues() {
    long total = 0;
    for (long cnt : frequencies) {
      total += cnt;
    }
    return total;
  }

  @Override
  public long getElementsInRange() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  public T getLowerBoundAt(int bucket) {
    if (bucket == 0) {
      return min;
    } else {
      return boundaries[bucket - 1];
    }
  }
  
  public T getUpperBoundAt(int bucket) {
    return boundaries[bucket];
  }
  
  public List<T> getLowerBounds() {
    List<T> lBounds = Lists.newArrayList();
    for (int i = 0; i < getNumberOfBuckets(); i++) {
      lBounds.add(getLowerBoundAt(i));
    }
    return lBounds;
  }
  
  public List<T> getUpperBounds() {
    return Arrays.asList(boundaries);
  }
  
}
