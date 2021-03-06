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

//import java.sql.Date;
//import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

public class OracleDateParser extends AbstractParser<oracle.sql.DATE> {
  
  private final static String DEFAULT_OUTPUT_FORMAT = "yyyy-mm-dd hh:mm:ss"; //"yyyy/mm/dd:hh:mi:ssam";
  
  private static final Logger LOGGER = Logger.getLogger(OracleDateParser.class);
  
  /*
   *   @Override
  public Date fromString(String value) {
    return Date.valueOf(removeQuotes(value));
  }

  @Override
  public String toString(Object value) {
    SimpleDateFormat outFormat = new SimpleDateFormat(DEFAULT_OUTPUT_FORMAT);
    return outFormat.format(value);
  }
   * */
  // TODO: test correctness
  @Override
  public oracle.sql.DATE fromString(String value) {
  	LOGGER.debug("entering OracleDateParser:fromString with input value = " + value);
	  oracle.sql.DATE date = new oracle.sql.DATE(value);
	  LOGGER.debug("leaving OracleDateParser:fromString with output = " + date.toString());
 	  return date;
  }

  @Override
  public String toString(Object value) {
  	LOGGER.debug("entering OracleDateParser:toString with input value = " + value.toString());
	  SimpleDateFormat outFormat = new SimpleDateFormat(DEFAULT_OUTPUT_FORMAT);
	  String outStr = outFormat.format(value);
	  LOGGER.debug("leaving OracleDateParser:toString with output value = " + outStr);
	  return outStr;
  }

}
