package de.tu_berlin.dima.oligos.stat.histogram;

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.stat.Bucket;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public class CustomHistogram<T> extends AbstractHistogram<T> {

  private SortedSet<T> lowerBounds;
  private SortedSet<T> upperBounds;
  private List<Long> frequencies;
  private Operator<T> operator;

  public CustomHistogram(Operator<T> operator) {
    this.operator = operator;
    this.lowerBounds = Sets.newTreeSet(operator);
    this.upperBounds = Sets.newTreeSet(operator);
    this.frequencies = Lists.newArrayList();
  }
  
  @Override
  public void add(T lowerBound, T upperBound, long frequency) {
    lowerBounds.add(lowerBound);
    upperBounds.add(upperBound);
    frequencies.add(frequency);
  }

  @Override
  public T getMin() {
    return operator.increment(lowerBounds.first());
  }

  @Override
  public T getMax() {
    return upperBounds.last();
  }

  @Override
  public int getNumberOfBuckets() {
    assert checkConsistence();
    return lowerBounds.size();
  }

  @Override
  public int getBucketOf(T value) {
    assert checkConsistence();
    Iterator<T> lBoundsIter = lowerBounds.iterator();
    Iterator<T> uBoundsIter = upperBounds.iterator();
    int index = 0;
    while (lBoundsIter.hasNext() && uBoundsIter.hasNext()) {
      T lBound = lBoundsIter.next();
      T uBound = uBoundsIter.next();
      operator.compare(lBound, value);
      if (operator.compare(lBound, value) < 0
          && operator.compare(uBound, value) <= 0) {
        return index;
      } else {
        index++;
      }
    }
    return -1;
  }

  @Override
  public long getTotalNumberOfValues() {
    long total = 0l;
    for (long freq : frequencies) {
      total += freq;
    }
    return total;
  }

  @Override
  public SortedSet<T> getLowerBounds() {
    return lowerBounds;
  }

  @Override
  public SortedSet<T> getUpperBounds() {
    return upperBounds;
  }

  @Override
  public List<Long> getFrequencies() {
    return frequencies;
  }
  
  @Override
  public Iterator<Bucket<T>> iterator() {
    return new Iterator<Bucket<T>>() {
      
      private final Iterator<T> lBoundsIter = lowerBounds.iterator();
      private final Iterator<T> uBoundsIter = upperBounds.iterator();
      private final Iterator<Long> freqIter = frequencies.iterator();

      @Override
      public boolean hasNext() {
        return lBoundsIter.hasNext() && uBoundsIter.hasNext() && freqIter.hasNext();
      }

      @Override
      public Bucket<T> next() {
        return new Bucket<T>(lBoundsIter.next(), uBoundsIter.next(), freqIter.next());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  public SortedMap<T, Long> getExactValues() {
    SortedMap<T, Long> exactVals = Maps.newTreeMap(operator);
    for (Bucket<T> bucket : this) {
      T lb = bucket.getLowerBound();
      T ub = bucket.getUpperBound();
      if (operator.compare(lb, ub) == 0) {
        exactVals.put(lb, bucket.getFrequency());
      }
    }
    return exactVals;
  }
  
  public void writeDomain() {
    // TODO write domain information in given format
  }

  private boolean checkConsistence() {
    return lowerBounds.size() == upperBounds.size()
        && lowerBounds.size() == frequencies.size()
        && upperBounds.size() == frequencies.size();
  }

}
