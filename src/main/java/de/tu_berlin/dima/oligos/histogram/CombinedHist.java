package de.tu_berlin.dima.oligos.histogram;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Preconditions;

import de.tu_berlin.dima.oligos.type.Operator;

public class CombinedHist<V extends Comparable<V>> implements
    Histogram<V> {

	private final static int IN_FHIST = -2;
	private final static int NOT_CONTAINED = -1;

	private final FHist<V> fHist;
	private V[] lowerBounds;
	private V[] upperBounds;
	private long[] frequencies;
	private Operator<V> operator;

	public CombinedHist(QHist<V> qHist, FHist<V> fHist) {
		this.fHist = fHist;
		this.operator = qHist.operator();
		adaptHistogram(qHist);
	}

	@SuppressWarnings("unchecked")
	private void adaptHistogram(QHist<V> qHist) {
		List<V> lBounds = new LinkedList<V>();
		List<V> uBounds = new LinkedList<V>(Arrays.asList(qHist.boundaries()));
		List<Long> freqs = new LinkedList<Long>();
		for (int i = 0; i < qHist.numBuckets(); i++) {
			lBounds.add(qHist.lowerBoundAt(i));
			uBounds.add(qHist.upperBoundAt(i));
			freqs.add(qHist.frequencyAt(i));
		}
		int index = 0;
		for (Entry<V, Long> e : fHist) {
			V elem = e.getKey();
			long count = e.getValue();
			boolean found = false;

			while (!found && index < freqs.size() - 1) {
				V lBound = lBounds.get(index);
				V uBound = uBounds.get(index);
				if (uBound.compareTo(elem) > 0) {
					if (lBound.equals(operator.decrement(uBound))) {
						lBounds.remove(index);
						uBounds.remove(index);
						freqs.remove(index);
						found = true;
					} else if (lBound.equals(elem)) {
						lBounds.set(index, operator.increment(elem));
						long freq = freqs.get(index) - count;
						freqs.set(index, freq);
						found = true;
					} else if (uBound.equals(operator.increment(elem))) {
						uBounds.set(index, elem);
						long freq = freqs.get(index) - count;
						freqs.set(index, freq);
						found = true;
						index++;
					} else {
						long freq = (freqs.get(index) - count) / 2;
						lBounds.set(index, operator.increment(elem));
						freqs.set(index, freq);
						lBounds.add(index, lBound);
						uBounds.add(index, elem);
						freqs.add(index, freq);
						found = true;
					}
				} else {
					index++;
				}
			}
		}
		Class<?> type = qHist.min().getClass();
		this.lowerBounds = (V[]) Array.newInstance(type, lBounds.size());
		this.lowerBounds = lBounds.toArray(this.lowerBounds);
		this.upperBounds = (V[]) Array.newInstance(type, uBounds.size());
		this.upperBounds = uBounds.toArray(this.upperBounds);
		this.frequencies = ArrayUtils.toPrimitive(freqs.toArray(new Long[0]));
	}
	
	@Override
	public String toString() {
		StringBuilder strBld = new StringBuilder();
		double cumProb = 0.0;
		// HEADER
		strBld.append("# numberofexactvals: ");
		strBld.append(fHist.numberOfBuckets());
		strBld.append('\n');
		strBld.append("# numberofbins: ");
		strBld.append(numBuckets());
		strBld.append('\n');
		strBld.append("# nullprobability: ");
		strBld.append(numberOfNulls() / (double) numElements());
		strBld.append('\n');
		for (Entry<V, Long> e : fHist) {
			double prob = probabilityOf(e.getKey());
			cumProb += prob;
			strBld.append(prob);
			strBld.append('\t');
			strBld.append(e.getKey());
			strBld.append('\n');
		}
		for (int i = 0; i < numBuckets(); i++) {
			double prob = frequencyAt(i) / (double) numElements();
			cumProb += prob;
			strBld.append(prob);
			strBld.append('\t');
			strBld.append(lowerBoundAt(i));
			strBld.append('\t');
			strBld.append(upperBoundAt(i));
			strBld.append('\n');
		}
		strBld.append(cumProb);

		return strBld.toString();
	}

	public String toString2() {
		StringBuilder strBld = new StringBuilder();
		strBld.append("\nHistogram\nNo\tLower Bound\tUpper Bound\tFrequency\t"
		    + "Cum. Frequency\tProbability\tCum. Probability\tCardinality\n");
		strBld.append("Num. Elements: " + numElements() + "\n");
		long cumFreq = 0l;
		double cumProb = 0.0;
		for (int i = 0; i < numBuckets(); i++) {
			strBld.append(i);
			strBld.append('\t');
			strBld.append(lowerBoundAt(i));
			strBld.append('\t');
			strBld.append(upperBoundAt(i));
			strBld.append('\t');
			strBld.append(frequencyAt(i));
			strBld.append('\t');
			cumFreq += frequencyAt(i);
			strBld.append(cumFreq);
			strBld.append('\t');
			double prob = frequencyAt(i) / (double) numElements();
			strBld.append(prob);
			strBld.append('\t');
			cumProb += prob;
			strBld.append(cumProb);
			strBld.append('\t');
			strBld.append(cardinalityAt(i));
			strBld.append('\n');
		}

		return strBld.toString();
	}

	public int numBuckets() {
		return frequencies.length;
	}

	@Override
	public long numElements() {
		long count = fHist.numElements();
		for (long freq : frequencies) {
			count += freq;
		}
		return count;
	}

	@Override
	public V min() {
		return (lowerBounds[0].compareTo(fHist.min()) <= 0) ? lowerBounds[0]
		    : fHist.min();
	}

	@Override
	public V max() {
		return (upperBounds[0].compareTo(fHist.max()) >= 0) ? upperBounds[upperBounds.length - 1]
		    : fHist.max();
	}

	@Override
	public long numberOfNulls() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public V lowerBoundAt(int index) {
		checkIndex(index);
		return lowerBounds[index];
	}

	@Override
	public V upperBoundAt(int index) {
		checkIndex(index);
		return upperBounds[index];
	}

	@Override
	public long frequencyAt(int index) {
		if (index == IN_FHIST) {
			return 0l;
		} else if (index == NOT_CONTAINED) {
			return 0l;
		} else {
			checkIndex(index);
			return frequencies[index];
		}
	}

	@Override
	public long cumFrequencyAt(int index) {
		checkIndex(index);
		return 0;
	}

	@Override
	public long cardinalityAt(int index) {
		if (index == IN_FHIST) {
			return 1l;
		} else if (index == NOT_CONTAINED) {
			return 0l;
		} else {
			return operator.difference(lowerBoundAt(index), upperBoundAt(index));			
		}
	}

	@Override
	public int indexOf(V value) {
		int i = 0;
		int len = numBuckets();
		if (fHist.contains(value)) {
			return IN_FHIST;
		}
		while (value.compareTo(upperBounds[i]) > 0) {
			if (i < len - 1) {
				i++;
			} else {
				i = NOT_CONTAINED;
				break;
			}
		}
		return i;
	}

	@Override
	public long frequencyOf(V value) {
		int index = indexOf(value);
		if (index == IN_FHIST) {
			return fHist.frequencyOf(value);
		} else if (index == NOT_CONTAINED) {
			return 0l;
		} else {
			checkIndex(index);
			return frequencyAt(index) / cardinalityAt(index);
		}
	}

	@Override
	public double probabilityOf(V value) {
		int index = indexOf(value);
		if (index == IN_FHIST) {
			return fHist.frequencyOf(value) / (double) numElements();
		} else if (index == NOT_CONTAINED) {
			return 0.0;
		} else {
			checkIndex(index);
			double bucketProb = frequencyAt(index) / numElements();
			double valueProb = 1.0 / cardinalityAt(index);
			return bucketProb * valueProb;
		}
	}

	private void checkIndex(int index) {
		Preconditions.checkArgument(index >= 0 && index < numBuckets(),
		    "No valid bucket index was given. (0 <= index < NUMBUCKETS)");
	}
}
