package de.tu_berlin.dima.oligos.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class DomainFileWriter extends AbstractFileWriter {
  
  @SuppressWarnings("serial")
  private final Set<String> myriadTypes = new HashSet<String>(){{
    add("integer");
    add("decimal");
    add("date");
  }};
  
  public DomainFileWriter(File outputDirectory, Map<String, Set<Column<?>>> relations, String extension) {
    super(outputDirectory, relations, extension);
  }

  @Override
  public void writeFile(File outputFile, Column<?> column) throws IOException {
    // TODO just check preconditions here and assume that input columns are already checked
    // for enumerated types, hence remove condition and replace by preconditions.check...
    if (isDomainType(column.getType()) && column.isEnumerated()) {
      Parser<?> parser = parserManager.getParser("", column.getTable(), column.getColumn());
      outputFile.mkdirs();
      outputFile.delete();
      outputFile.createNewFile();
      OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
      BufferedWriter bufWriter = new BufferedWriter(out);
      Map<?, Long> domain = column.getDomain();
      long cardinality = column.getCardinality();
      bufWriter.write("@numberofvalues = " + cardinality + "\n");
      int i = 0;
      for (Object element : domain.keySet()) {
        String value = parser.toString(element);
        bufWriter.write(i + "\t" + value + "\n");
        i++;
      }
      bufWriter.close();
    }
  }
  
  private boolean isDomainType(String typeName) {
    return !myriadTypes.contains(typeName);
  }

}
