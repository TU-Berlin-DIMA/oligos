package de.tu_berlin.dima.oligos.stat.distribution.histogram;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class StringHistogram implements Histogram<String> {
  
  private final SortedMap<String, Long> exactValues;
  private long total;
  
  public StringHistogram() {
    this.exactValues = Maps.newTreeMap();
    this.total = 0l;
  }

  public StringHistogram(Map<String, Long> mappings) {
    this();
    this.exactValues.putAll(mappings);
    this.total = mappings.size();
  }

  @Override
  public String getMin() {
    return exactValues.firstKey();
  }

  @Override
  public String getMax() {
    return exactValues.lastKey();
  }

  @Override
  public long getCardinality() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getProbabilityOf(String value) {
    if (exactValues.containsKey(value)) {
      long count = exactValues.get(value);
      return (double) count / total;
    } else {
      return 0.0;
    }
  }

  @Override
  public Iterator<Bucket<String>> iterator() {
    return new Iterator<Bucket<String>>() {
      private final Iterator<Entry<String, Long>> entryIter =
          exactValues.entrySet().iterator();

      @Override
      public boolean hasNext() {
        return entryIter.hasNext();
      }

      @Override
      public Bucket<String> next() {
        Entry<String, Long> e = entryIter.next();
        String value = e.getKey();
        long count = e.getValue();
        return new Bucket<String>(value, value, count);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public void add(String lowerBounds, String upperBound, long frequency) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getNumberOfBuckets() {
    return exactValues.size();
  }

  @Override
  public int getBucketOf(String value) {
    int index = 0;
    for (String current : exactValues.keySet()) {
      if (value.equals(current)) {
        break;
      }
      index++;
    }
    return index;
  }

  @Override
  public long getTotalNumberOfValues() {
    return total;
  }

  @Override
  public Histogram<String> getExactValues() {
    return this;
  }

  @Override
  public Histogram<String> getNonExactValues() {
    return new StringHistogram();
  }

  @Override
  public SortedSet<String> getLowerBounds() {
    return Sets.newTreeSet(exactValues.keySet());
  }

  @Override
  public SortedSet<String> getUpperBounds() {
    return Sets.newTreeSet(exactValues.keySet());
  }

  @Override
  public List<Long> getFrequencies() {
    List<Long> freqs = Lists.newArrayList(exactValues.values());
    return freqs;
  }

  @Override
  public boolean isEmpty() {
    return exactValues.isEmpty();
  }

}
