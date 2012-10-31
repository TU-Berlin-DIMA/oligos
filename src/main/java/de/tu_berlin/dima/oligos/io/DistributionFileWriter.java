package de.tu_berlin.dima.oligos.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.tu_berlin.dima.oligos.stat.Bucket;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.histogram.Histogram;
import de.tu_berlin.dima.oligos.stat.histogram.Histograms;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class DistributionFileWriter extends AbstractFileWriter {

  @SuppressWarnings("serial")
  private final Set<String> myriadTypes = new HashSet<String>(){{
    add("integer");
    add("decimal");
    add("date");
  }};
  
  public DistributionFileWriter(File outputDirectory, Map<String, Set<Column<?>>> relations, String extension) {
    super(outputDirectory, relations, extension);
  }

  @Override
  public void writeFile(File outputFile, Column<?> column) throws IOException {
    long numTotalValues = column.getNumberOfValues();
    Map<?, Long> mostFrequent;
    Histogram<?> distribution = column.getDistribution();
    if (column.isEnumerated()) {
      mostFrequent = column.getDomain();
    } else {
      mostFrequent = Histograms.getMostFrequent(column.getDistribution());
    }
    int numExactVals = mostFrequent.size();
    int numBuckets = distribution.getNumberOfBuckets();
    double nullProb = column.getNumNulls() / (double) numTotalValues;
    
    Parser<?> parser = parserManager.getParser("", column.getTable(), column.getColumn());
    //Operator<?> operator = TypeManager.getInstance().getOperator("", column.getTable(), column.getColumn());
    outputFile.mkdirs();
    outputFile.delete();
    outputFile.createNewFile();
    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
    BufferedWriter bufWriter = new BufferedWriter(out);
    bufWriter.write("@numberofexactvals = " + numExactVals + "\n");
    bufWriter.write("@numberofbins = " + numBuckets + "\n");
    bufWriter.write("@nullprobability = " + nullProb + "\n");
    int index = 0;
    for (Entry<?, Long> entry : mostFrequent.entrySet()) {
      String value = parser.toString(entry.getKey());
      double probability = entry.getValue() / (double) numTotalValues;
      if (isDomainType(column.getType())) {
        bufWriter.write("p(X) = " + probability + "\t" + "for X = {" + index
            + "}\t# " + parser.toString(entry.getKey()) + "\n");
      } else {
        bufWriter.write("p(X) = " + probability + "\t" + "for X = {" + value
            + "}\n");
      }
      index++;
    }
    for (Bucket<?> bucket : distribution) {
      /*
       * p(X) = 0.05   for X = { x in [1.0    , 10.0   ) } # between 1 and 10 euro
       * p(X) = 0.15   for X = { x in [10.0   , 100.0  ) } # between 10 and 100 euro
       */
      double probability = bucket.getFrequency() / (double) numTotalValues;
      String lowerBound = parser.toString(bucket.getLowerBound());
      String upperBound = parser.toString(bucket.getUpperBound());
      bufWriter.write("p(X) = " + probability + "\t" + "for X = { x in [" + lowerBound + ", " + upperBound + ")}\n");
    }
    bufWriter.close();
  }
  
  private boolean isDomainType(String typeName) {
    return !myriadTypes.contains(typeName);
  }

}
