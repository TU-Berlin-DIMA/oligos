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

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DateParser extends AbstractParser<Date> {
  
  private final static String DEFAULT_OUTPUT_FORMAT = "yyyy-MM-dd";
  
  @Override
  public Date fromString(String value) {
    return Date.valueOf(removeQuotes(value));
  }

  @Override
  public String toString(Object value) {
    SimpleDateFormat outFormat = new SimpleDateFormat(DEFAULT_OUTPUT_FORMAT);
    return outFormat.format(value);
  }

}
