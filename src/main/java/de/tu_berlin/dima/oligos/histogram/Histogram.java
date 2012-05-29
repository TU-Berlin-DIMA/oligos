package de.tu_berlin.dima.oligos.histogram;

public interface Histogram<V extends Comparable<V>> {
	/*
	 * Domain information
	 */
	/**
	 * Obtain the total number of elements within the histogram.
	 */
	public long numElements();

	public V min();

	public V max();

	public long numberOfNulls();

	/*
	 * Bucket methods
	 */
	public V lowerBoundAt(int index);

	public V upperBoundAt(int index);

	public long frequencyAt(int index);

	public long cumFrequencyAt(int index);

	public long cardinalityAt(int index);

	/*
	 * Value methods
	 */
	public int indexOf(V value);

	public long frequencyOf(V value);

	public double probabilityOf(V value);
}
