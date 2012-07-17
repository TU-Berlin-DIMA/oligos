package de.tu_berlin.dima.oligos.type.db2;

import java.math.BigDecimal;
import java.util.Date;

import de.tu_berlin.dima.oligos.histogram.BucketHistogram;
import de.tu_berlin.dima.oligos.histogram.CombinedHist;
import de.tu_berlin.dima.oligos.histogram.ElementHistogram;
import de.tu_berlin.dima.oligos.histogram.FHist;
import de.tu_berlin.dima.oligos.histogram.QHist;
import de.tu_berlin.dima.oligos.type.AbstractTypeFactory;
import de.tu_berlin.dima.oligos.type.DateType;
import de.tu_berlin.dima.oligos.type.DecimalType;
import de.tu_berlin.dima.oligos.type.IntegerType;
import de.tu_berlin.dima.oligos.type.Type;

public class DB2TypeFactory extends AbstractTypeFactory {

  @Override
  public Type<?> createType(Class<?> type, String value) {
    Type<?> instance = null;

    if (type.equals(Short.class)) {
      throw new UnsupportedOperationException("Not yet supported: "
          + type.getSimpleName());
    }

    else if (type.equals(Integer.class)) {
      instance = new IntegerType(value);
    }

    else if (type.equals(Long.class)) {
      throw new UnsupportedOperationException("Not yet supported: "
          + type.getSimpleName());
    }

    else if (type.equals(Float.class)) {
      throw new UnsupportedOperationException("Not yet supported: "
          + type.getSimpleName());
    }

    else if (type.equals(Double.class)) {
      throw new UnsupportedOperationException("Not yet supported: "
          + type.getSimpleName());
    }

    else if (type.equals(Date.class)) {
      instance = new DateType(value);
    }

    else if (type.equals(BigDecimal.class)) {
      instance = new DecimalType(value);
    }

    else if (type.equals(String.class)) {
      throw new UnsupportedOperationException("Not yet supported: "
          + type.getSimpleName());
    }

    return instance;
  }

  @Override
  public BucketHistogram<?> createBucketHistogram(Type<?>[] boundaries,
      long[] frequencies, Type<?> min, long cardinality, long numNulls) {
    BucketHistogram<?> histogram = null;

    if (min.getClass().equals(IntegerType.class)) {
      IntegerType m = (IntegerType) min;
      IntegerType[] b = new IntegerType[boundaries.length];
      System.arraycopy(boundaries, 0, b, 0, boundaries.length);
      histogram = new QHist<IntegerType>(b, frequencies, m, cardinality,
          numNulls);
    }

    else if (min.getClass().equals(DateType.class)) {
      DateType m = (DateType) min;
      DateType[] b = new DateType[boundaries.length];
      System.arraycopy(boundaries, 0, b, 0, boundaries.length);
      histogram = new QHist<DateType>(b, frequencies, m, cardinality, numNulls);
    }

    else if (min.getClass().equals(DecimalType.class)) {
      DecimalType m = (DecimalType) min;
      DecimalType[] b = new DecimalType[boundaries.length];
      System.arraycopy(boundaries, 0, b, 0, boundaries.length);
      histogram = new QHist<DecimalType>(b, frequencies, m, cardinality,
          numNulls);
    }

    return histogram;
  }

  @Override
  public ElementHistogram<?> createElementHistogram(Type<?>[] values,
      long[] frequencies) {
    ElementHistogram<?> histogram = null;
    Type<?> rand = values[0];

    if (rand.getClass().equals(IntegerType.class)) {
      IntegerType[] v = new IntegerType[values.length];
      System.arraycopy(values, 0, v, 0, values.length);
      histogram = new FHist<IntegerType>(v, frequencies);
    }

    else if (rand.getClass().equals(DecimalType.class)) {
      DecimalType[] v = new DecimalType[values.length];
      System.arraycopy(values, 0, v, 0, values.length);
      histogram = new FHist<DecimalType>(v, frequencies);
    }

    else if (rand.getClass().equals(DateType.class)) {
      DateType[] v = new DateType[values.length];
      System.arraycopy(values, 0, v, 0, values.length);
      histogram = new FHist<DateType>(v, frequencies);
    }

    return histogram;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public CombinedHist<?> createCombinedHistogram(ElementHistogram<?> eHist, BucketHistogram<?> bHist) {
    Class<?> eType = eHist.min().getClass();
    Class<?> bType = bHist.min().getClass();
    CombinedHist<?> histogram = null;
    
    if (!eType.equals(bType)) {
      throw new RuntimeException();
    }
    
    if (eType.equals(IntegerType.class)) {
      BucketHistogram<IntegerType> b = (BucketHistogram<IntegerType>) bHist;
      ElementHistogram<IntegerType> e = (ElementHistogram<IntegerType>) eHist;
      histogram = new CombinedHist<IntegerType>(b, e);
    }
    
    else if (eType.equals(DecimalType.class)) {
      BucketHistogram<DecimalType> b = (BucketHistogram<DecimalType>) bHist;
      ElementHistogram<DecimalType> e = (ElementHistogram<DecimalType>) eHist;
      histogram = new CombinedHist<DecimalType>(b, e);
    }
    
    else if (eType.equals(DateType.class)) {
      BucketHistogram<DateType> b = (BucketHistogram<DateType>) bHist;
      ElementHistogram<DateType> e = (ElementHistogram<DateType>) eHist;
      histogram = new CombinedHist<DateType>(b, e);
    }
    
    return histogram;
  }

}
