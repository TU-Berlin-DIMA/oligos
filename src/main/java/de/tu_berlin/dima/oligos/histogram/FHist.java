package de.tu_berlin.dima.oligos.histogram;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class FHist<V extends Comparable<V>> implements Histogram<V>, Iterable<Entry<V, Long>> {

  private SortedMap<V, Long> frequentElements;
  private long total;
  
  public FHist() {
    this.frequentElements = new TreeMap<V, Long>();
		this.total = 0l;
  }
  
  public void addFrequentElement(final V elem, final long count) {
    frequentElements.put(elem, count);
    total += count;
  }

  public Iterator<Entry<V, Long>> iterator() {
    return frequentElements.entrySet().iterator();
  }
  
  public String toString() {
    StringBuilder strBld = new StringBuilder();
    for (Entry<V, Long> e : frequentElements.entrySet()) {
      strBld.append(probabilityOf(e.getKey()));
      strBld.append('\t');
      strBld.append(e.getValue());
      strBld.append('\n');
    }
    
    return strBld.toString();
  }
  
  public int numberOfBuckets() {
  	return frequentElements.size();
  }

	@Override
  public long numElements() {
	  return total;
  }

	@Override
  public V min() {
		return frequentElements.firstKey();
  }

	@Override
  public V max() {
	  return frequentElements.lastKey();
  }

	@Override
  public long numberOfNulls() {
	  return 0;
  }
	
	public boolean contains(V value) {
		return frequentElements.containsKey(value);
	}

	@Override
  public V lowerBoundAt(int index) {
	  throw new UnsupportedOperationException();	  
  }

	@Override
  public V upperBoundAt(int index) {
		throw new UnsupportedOperationException();
  }

	@Override
  public long frequencyAt(int index) {
	  throw new UnsupportedOperationException();
  }

	@Override
  public long cumFrequencyAt(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
  public long cardinalityAt(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
  public int indexOf(V value) {
		throw new UnsupportedOperationException();
	}

	@Override
  public long frequencyOf(V value) {
		Long freq = frequentElements.get(value);
		if (freq == null) {
			freq = 0l;
		}
		return freq;
	}

	@Override
  public double probabilityOf(V value) {
		return frequencyOf(value) / (double) numElements();
  }
}
