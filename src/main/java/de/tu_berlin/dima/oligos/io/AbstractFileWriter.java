package de.tu_berlin.dima.oligos.io;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.type.util.operator.OperatorManager;
import de.tu_berlin.dima.oligos.type.util.parser.ParserManager;

public abstract class AbstractFileWriter implements Writer {
  
  private final File outputDirectory;
  private final Map<String, Set<Column<?>>> relations;
  private final String extension;
  protected final ParserManager parserManager;
  protected final OperatorManager operatorManager;

  public AbstractFileWriter(File outputDirectory, Map<String, Set<Column<?>>> relations, String extension) {
    this.outputDirectory = outputDirectory;
    this.relations = relations;
    this.extension = extension;
    this.parserManager = ParserManager.getInstance();
    this.operatorManager = OperatorManager.getInstance();
  }

  @Override
  public void write() throws IOException {
    for (Entry<String, Set<Column<?>>> e : relations.entrySet()) {
      String table = e.getKey().toLowerCase();
      Set<Column<?>> columns = e.getValue();
      File tableDir = new File(outputDirectory, table);
      for (Column<?> column : columns) {
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
