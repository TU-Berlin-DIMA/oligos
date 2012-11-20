package de.tu_berlin.dima.oligos.stat.distribution.histogram;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public abstract class Histograms {

  public static <T> Histogram<T> combineHistograms(Histogram<T> hist, Map<T, Long> mostFrequent, Operator<T> operator) {
    Histogram<T> histogram = new CustomHistogram<T>(operator);
    // Make a deep copy to keep function side effect free
    mostFrequent = Maps.newHashMap(mostFrequent);
    // generate histogram with one sized buckets
    if (hist.isEmpty()) {
      for (Entry<T, Long> e : mostFrequent.entrySet()) {
        T value = e.getKey();
        long count = e.getValue();
        histogram.add(value, value, count);
      }
    }
    for (Bucket<T> bucket : hist) {
      SortedSet<T> elemsInRange = collectElementsInRange(bucket, mostFrequent, operator);
      // sum the most frequent elements in range
      long sumInRange = 0l;
      for (T e : elemsInRange) {
        sumInRange += mostFrequent.get(e);
      }
      // adapt the frequency count of the current bucket
      // i.e. subtract the number of most frequent elements
      bucket = new Bucket<T>(bucket.getLowerBound(), bucket.getUpperBound(), bucket.getFrequency() - sumInRange);
      
      // adapt the bucket
      // i.e. change the boundaries, introduce new buckets, ...
      for (T elem : elemsInRange) {
        T lBound = bucket.getLowerBound();
        T uBound = bucket.getUpperBound();
        long elemCnt = mostFrequent.get(elem);
        // bucket has exact one element and this is the most frequent
        if (lBound.equals(uBound) && lBound.equals(elem)) {
          histogram.add(lBound, uBound, elemCnt);
          mostFrequent.remove(elem);
        }
        // the most frequent element is the lower bound of the current bucket
        else if (lBound.equals(elem)) {
          histogram.add(lBound, elem, elemCnt);
          bucket = new Bucket<T>(operator.increment(lBound), uBound, bucket.getFrequency());
          mostFrequent.remove(elem);
        }
        // the most frequent element is the upper bound of the current bucket
        else if (uBound.equals(elem)) {
          histogram.add(lBound, operator.decrement(uBound), bucket.getFrequency());
          histogram.add(elem, elem, elemCnt);
          mostFrequent.remove(elem);
        }
        // common case, that the most frequent value is within the current bucket
        else {
          // shrink the current bucket and add shrunk bucket and most frequent element to histogram
          long range = operator.range(lBound, uBound);
          long lowerSize = operator.range(lBound, elem);
          long lowerFreq = lowerSize * bucket.getFrequency() / range;
          histogram.add(lBound, operator.decrement(elem), lowerFreq);
          histogram.add(elem, elem, elemCnt);
          long upperSize = operator.range(operator.increment(elem), uBound);
          long upperFreq = upperSize * bucket.getFrequency() / range;
          bucket = new Bucket<T>(operator.increment(elem), uBound, upperFreq);
          mostFrequent.remove(elem);
        }
      }
      histogram.add(bucket.getLowerBound(), bucket.getUpperBound(), bucket.getFrequency());
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
  
  public static <T> Map<T, Long> getMostFrequent(Histogram<T> histogram) {
    Map<T, Long> mostFrequent = Maps.newLinkedHashMap();
    for (Bucket<T> buck : histogram) {
      if (buck.getLowerBound().equals(buck.getUpperBound())) {
        mostFrequent.put(buck.getLowerBound(), buck.getFrequency());
      }
    }
    return mostFrequent;
  }

}
