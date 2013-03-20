package de.tu_berlin.dima.oligos.io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.Bucket;
import de.tu_berlin.dima.oligos.stat.distribution.histogram.Histogram;
import de.tu_berlin.dima.oligos.type.util.operator.Operators;

public class DistributionWriter implements Writer {

  @SuppressWarnings("serial")
  private final static Set<String> QUOTED_TYPE = new HashSet<String>() {{
    add("char");
  }};

  private final File distributionFile;
  private final File domainFile;
  private final Column<?> column;

  public DistributionWriter(final Column<?> column, final File distributionFile
      , final File domainFile) {
    // TODO create parent directory
    this.distributionFile = distributionFile;
    this.domainFile = domainFile;
    this.column = column;
  }

  public String getDomainString() {
    long cardinality = column.getCardinality();
    Histogram<?> exactValues = column.getDistribution().getExactValues();
    StringBuilder strBld = new StringBuilder(getDomainFileHeader(cardinality));
    strBld.append('\n');
    int index = 0;
    for (Bucket<?> exactBucket : exactValues) {
      strBld.append(index++);
      strBld.append('\t');
      strBld.append(column.asString(exactBucket.getLowerBound()));
      strBld.append('\n');
    }
    return strBld.toString();
  }

  public String getDistributionString() {
    StringBuilder strBld = new StringBuilder();
    boolean isEnum = column.isEnumerated();
    Histogram<?> distribution = column.getDistribution();
    Histogram<?> exactValues = distribution.getExactValues();
    long numTotal = column.getNumberOfRecords();
    int numExactVals = exactValues.getNumberOfBuckets();
    int numBuckets = distribution.getNumberOfBuckets() - numExactVals;
    //double nullProbability = column.getNumNulls() / (double) numTotal;
    double nullProbability = column.getNullProbability();
    strBld.append(getDistributionFileHeader(numExactVals, numBuckets, nullProbability));
    strBld.append('\n');
    strBld.append("# exact values");
    strBld.append('\n');
    int index = 0;
    for (Bucket<?> exactBucket : exactValues) {
      double probability = exactBucket.getFrequency() / (double) numTotal;
      String value = column.asString(exactBucket.getLowerBound());
      if (isEnum) {
        strBld.append(getExactEntry(probability, value, index++));
      } else {
        if (QUOTED_TYPE.contains(column.getType())) {
          value = "\'" + value + "\'";
        }
        strBld.append(getExactEntry(probability, value));
      }
      strBld.append('\n');
    }
    strBld.append("# bucket probabilities");
    strBld.append('\n');
    for (Bucket<?> bucket : distribution.getNonExactValues()) {
      String lowerBound = column.asString(bucket.getLowerBound());
      String upperBound = column.asString(Operators.increment(bucket.getUpperBound()));
      double probability = bucket.getFrequency() / (double) numTotal;
      if (!isEnum) {
        strBld.append(getBucketEntry(probability, lowerBound, upperBound));
        strBld.append('\n');
      }
    }
    return strBld.toString();
  }

  public File createFile() {
    return null;
  }

  @Override
  public void write() throws IOException {
    distributionFile.mkdirs();
    distributionFile.delete();
    distributionFile.createNewFile();
    Files.write(getDistributionString(), distributionFile, Charsets.UTF_8);
    if (column.isEnumerated()) {
      domainFile.mkdirs();
      domainFile.delete();
      domainFile.createNewFile();
      Files.write(getDomainString(), domainFile, Charsets.UTF_8);
    }
  }

  private String getDomainFileHeader(long numValues) {
    StringBuilder strBld = new StringBuilder();
    strBld.append("@numberofvalues = ");
    strBld.append(numValues);
    return strBld.toString();
  }

  private String getDistributionFileHeader(int numExactVals, int numBuckets
      , double nullProbability) {
    StringBuilder strBld = new StringBuilder();
    strBld.append("@numberofexactvals = ");
    strBld.append(numExactVals);
    strBld.append('\n');
    strBld.append("@numberofbins = ");
    strBld.append(numBuckets);
    strBld.append('\n');
    strBld.append("@nullprobability = ");
    strBld.append(nullProbability);
    return strBld.toString();
  }

  private String getExactEntry(double probability, String value) {
    StringBuilder strBld = new StringBuilder();
    strBld.append("p(X) = ");
    strBld.append(String.valueOf(probability));
    strBld.append('\t');
    strBld.append("for X = { ");
    strBld.append(value);
    strBld.append(" }");
    return strBld.toString();
  }

  private String getExactEntry(double probability, String value, int index) {
    StringBuilder strBld = new StringBuilder(getExactEntry(probability, String.valueOf(index)));
    strBld.append(" # ");
    strBld.append(value);
    return strBld.toString();
  }

  private String getBucketEntry(double probability, String lowerBound, String upperBound) {
    StringBuilder strBld = new StringBuilder();
    strBld.append("p(X) = ");
    strBld.append(String.valueOf(probability));
    strBld.append('\t');
    strBld.append("for X = { ");
    strBld.append("x in [");
    strBld.append(lowerBound);
    strBld.append(", ");
    strBld.append(upperBound);
    strBld.append(")");
    strBld.append(" }");
    return strBld.toString();
  }

}
