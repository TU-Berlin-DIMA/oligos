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
package de.tu_berlin.dima.oligos.type.util.operator.date;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

import org.apache.log4j.Logger;

import java.sql.SQLException;

// TODO test operator functions
public class OracleDateOperator implements Operator<oracle.sql.DATE> {

  private static final Logger LOGGER = Logger.getLogger(OracleDateOperator.class);
	
  
  public OracleDateOperator(){
  	LOGGER.warn("deprecated: use java.sql.Timestamp type and operator instead");
  }
	@Override
	public int compare(oracle.sql.DATE arg0, oracle.sql.DATE arg1) {
		return arg0.compareTo(arg1);
	}

	@Override
	/* 
	 * Add one second. Note subsequent increment function is different from 
	 * java.sql.date's increment function which adds one day.
	 * Change to add(1,0) in order to add a day instead of a second. 
	 */
	public oracle.sql.DATE increment(oracle.sql.DATE value) throws SQLException {
		return value.addJulianDays(0,1);
	}

	@Override
	/*
	 * Substract one second.
	 */
	public oracle.sql.DATE decrement(oracle.sql.DATE value) throws SQLException {
		return value.addJulianDays(0, -1);
	}

	@Override
	// TODO check semantics of diff computation
	public long range(oracle.sql.DATE val1, oracle.sql.DATE val2) throws SQLException {
		//Time t1 = val1.timeValue();
		//t1.getDay()
		long diff = val2.longValue() - val1.longValue();
    long days = (diff / (1000 * 60 * 60 * 24));
    return days;
   }

	@Override
	public oracle.sql.DATE min(oracle.sql.DATE val1, oracle.sql.DATE val2) {
		LOGGER.trace("entering OracleDateOperator:min(oracle.sql.DATE, oracle.sql.DATE)");
		if (val1.compareTo(val2) <= 0)
			return val1;
		return val2;
	}
	
	/*
	public oracle.sql.DATE min(java.sql.Timestamp val1, oracle.sql.DATE val2) {
		if (this.LOGGER != null) this.LOGGER.trace("entering OracleDateOperator:min(java.sql.Timestamp, oracle.sql.DATE)");
		oracle.sql.DATE val1_DATE = new oracle.sql.DATE(val1.toString().substring(0, 19));
		if (this.LOGGER != null) this.LOGGER.trace("casted timestamp value = " + val1.toString() +" to oracle.date = " + val1_DATE.toString());
		if (val1_DATE.compareTo(val2) <= 0)
			return val1_DATE;
		return val2;
	}*/
	

	@Override
	public oracle.sql.DATE max(oracle.sql.DATE val1, oracle.sql.DATE val2) {
		if (val1.compareTo(val2) >= 0)
			return val1;
		return val2;
	}

}
