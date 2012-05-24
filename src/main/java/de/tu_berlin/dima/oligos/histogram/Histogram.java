package de.tu_berlin.dima.oligos.histogram;

public interface Histogram {
  void addPoint(int value);
  int getBucketIndex(int value);
  int getLowerBound(int value);
  int getLoweBoundAt(int index);
  int getUpperBound(int value);
  int getUpperBoundAt(int index);
  int getAbsoluteFrequency(int value);
  int getRelativeFrequency(int value);
}
