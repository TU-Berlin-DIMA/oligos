/*******************************************************************************
 * Copyright 2013 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
