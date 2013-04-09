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
package de.tu_berlin.dima.oligos.type.util.parser;

import java.util.Map;

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.type.util.ColumnId;

public class ParserManager {

  private final Map<ColumnId, Parser<?>> parsers;
  
  public ParserManager() {
    this.parsers = Maps.newHashMap();
  }
  
  public void register(ColumnId columnId, Parser<?> parser) {
    parsers.put(columnId, parser);
  }

  public void register(String schema, String table, String column, Parser<?> parser) {
    ColumnId col = new ColumnId(schema, table, column);
    register(col, parser);
  }
  
  public Parser<?> getParser(String schema, String table, String column) {
    ColumnId col = new ColumnId(schema, table, column);
    return getParser(col);
  }
  
  public Parser<?> getParser(ColumnId columnId) {
    return parsers.get(columnId);
  }
}
