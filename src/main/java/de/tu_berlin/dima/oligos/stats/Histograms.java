package de.tu_berlin.dima.oligos.stats;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public abstract class Histograms {

  public static <T> Histogram<T> combineHistograms(Histogram<T> hist,
      Map<T, Long> mostFrequent, Operator<T> operator) {
    hist = updateFrequencies(hist, mostFrequent, operator);
    hist = updateBoundaries(hist, mostFrequent, operator);
    return hist;
  }

  public static <T> Histogram<T> updateFrequencies(Histogram<T> hist,
      Map<T, Long> mostFrequent, Operator<T> operator) {
    Histogram<T> histogram = new GenericHistogram<T>(operator);
    for (Bucket<T> bucket : hist) {
      T lBound = bucket.getLowerBound();
      T uBound = bucket.getUpperBound();
      long exactCounts = 0l;
      // count the most frequent elements that are in the current bucket
      for (Entry<T, Long> e : mostFrequent.entrySet()) {
        T value = e.getKey();
        long count = e.getValue();
        if (isInBucket(lBound, uBound, value, operator)) {
          exactCounts += count;
        }
      }
      histogram.add(lBound, uBound, bucket.getFrequency() - exactCounts);
    }
    return histogram;
  }

  public static <T> Histogram<T> updateBoundaries(Histogram<T> hist,
      Map<T, Long> mostFrequent, Operator<T> operator) {
    Histogram<T> histogram = new GenericHistogram<T>(operator);
    // Make a deep copy to keep function side effect free
    mostFrequent = Maps.newHashMap(mostFrequent);
    // List<Bucket<T>> newBuckets = Lists.newArrayList();
    for (Bucket<T> bucket : hist) {
      SortedSet<T> mostFreqs = collectElementsInRange(bucket, mostFrequent,
          operator);
      long div = operator.difference(bucket.getLowerBound(),
          bucket.getUpperBound()) + 1;
      if (mostFreqs.isEmpty()) {
        histogram.add(bucket.getLowerBound(), bucket.getUpperBound(),
            bucket.getFrequency());
      } else {
        div -= mostFreqs.size();
        for (T elem : mostFreqs) {
          if (isLowerBound(bucket, elem, operator)) {
            histogram.add(elem, elem, mostFrequent.get(elem));
            bucket = new Bucket<T>(operator.increment(elem), bucket.getUpperBound(), bucket.getFrequency());
          } else if (isUpperBound(bucket, elem, operator)) {
            div = 1l;
            histogram.add(bucket.getLowerBound(), operator.decrement(elem), bucket.getFrequency());
            bucket = new Bucket<T>(elem, elem, mostFrequent.get(elem));
          } else if (operator.difference(bucket.getLowerBound(),
              bucket.getUpperBound()) + 1 == 1) {
            bucket = new Bucket<T>(elem, elem, mostFrequent.get(elem));
          } else {
            long mul = operator.difference(bucket.getLowerBound(), elem) + 1;
            long freq = bucket.getFrequency();
            histogram.add(bucket.getLowerBound(), operator.decrement(elem), mul
                * freq / div);
            histogram.add(elem, elem, mostFrequent.get(elem));
            bucket = new Bucket<T>(operator.increment(elem),
                bucket.getUpperBound(), freq);
          }
          // remove the element as there could not be any other bucket
          // containing it
          mostFrequent.remove(elem);
        }
        long mul = operator.difference(bucket.getLowerBound(), bucket.getUpperBound()) + 1;
        histogram.add(bucket.getLowerBound(), bucket.getUpperBound(), mul * bucket.getFrequency() / div);
      }
    }
    return histogram;
  }
  
  public static <T> SortedSet<T> collectElementsInRange(Bucket<T> bucket, Map<T, Long> mostFrequent, Operator<T> operator) {
    SortedSet<T> elemsInRange = Sets.newTreeSet(operator);
    for (T elem : mostFrequent.keySet()) {
      if (isInBucket(bucket, elem, operator)) {
        elemsInRange.add(elem);
      }
    }
    return elemsInRange;
  }
  
  public static <T> boolean isInBucket(Bucket<T> bucket, T value, Operator<T> operator) {
    return isInBucket(bucket.getLowerBound(), bucket.getUpperBound(), value, operator);
  }

  public static <T> boolean isInBucket(T lowerBound, T upperBound, T value,
      Operator<T> operator) {
    return operator.compare(lowerBound, value) <= 0
        && operator.compare(value, upperBound) <= 0;
  }
  
  public static <T> boolean isLowerBound(Bucket<T> bucket, T value, Operator<T> operator) {
    return equals(bucket.getLowerBound(), value, operator);
  }
  
  public static <T> boolean isUpperBound(Bucket<T> bucket, T value, Operator<T> operator) {
    return equals(bucket.getUpperBound(), value, operator);
  }

  public static <T> boolean equals(T other, T value, Operator<T> operator) {
    return operator.compare(other, value) == 0;
  }

  public static <T> List<T> collectValuesInRange(Map<T, Long> mostFrequent,
      T lowerBound, T upperBound, Operator<T> operator) {
    List<T> vals = Lists.newArrayList();
    for (T val : mostFrequent.keySet()) {
      if (isInBucket(lowerBound, upperBound, val, operator)) {
        vals.add(val);
      }
    }
    return vals;
  }

}
