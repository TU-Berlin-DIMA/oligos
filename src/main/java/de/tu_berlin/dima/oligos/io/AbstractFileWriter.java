package de.tu_berlin.dima.oligos.io;

import java.io.File;
import java.io.IOException;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.Schema;
import de.tu_berlin.dima.oligos.stat.Table;

public abstract class AbstractFileWriter implements Writer {
  
  private final File outputDirectory;
  private final Schema schema;
  private final String extension;

  public AbstractFileWriter(File outputDirectory, Schema schema, String extension) {
    this.outputDirectory = outputDirectory;
    this.schema = schema;
    this.extension = extension;
  }

  @Override
  public void write() throws IOException {
    for (Table table : schema) {
      File tableDir = new File(outputDirectory, table.getTable());
      for (Column<?> column : table) {
        File outputFile = new File(tableDir, getFileName(column));
        writeFile(outputFile, column);
      }
    }
  }

  private String getFileName(Column<?> column) {
    return column.getColumn() + "." + extension;
  }

  public abstract void writeFile(File outputFile, Column<?> column) throws IOException;

}
