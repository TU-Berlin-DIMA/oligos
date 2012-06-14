package de.tu_berlin.dima.oligos.type;

import de.tu_berlin.dima.oligos.histogram.BucketHistogram;
import de.tu_berlin.dima.oligos.histogram.CombinedHist;
import de.tu_berlin.dima.oligos.histogram.ElementHistogram;
import de.tu_berlin.dima.oligos.type.db2.DB2TypeFactory;

public abstract class AbstractTypeFactory {

  public AbstractTypeFactory getTypeFactory(String vendor) {
    if (vendor.equalsIgnoreCase("db2")) {
      DateType.setInputFormat("yyyy-MM-dd");
      return new DB2TypeFactory();
    }

    return null;
  }

  public abstract Type<?> createType(Class<?> type, String value);

  public abstract ElementHistogram<?> createElementHistogram(Type<?>[] values,
      long[] frequencies);

  public abstract BucketHistogram<?> createBucketHistogram(
      Type<?>[] boundaries, long[] frequencies, Type<?> min, long cardinality,
      long numNulls);

  public abstract CombinedHist<?> createCombinedHistogram(ElementHistogram<?> eHist,
      BucketHistogram<?> bHist);

}
