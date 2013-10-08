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
package de.tu_berlin.dima.oligos.db.oracle;

import java.sql.SQLException;
import java.util.Set;

import org.javatuples.Quartet;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.SchemaConnector;


import org.apache.log4j.Logger;

public class OracleSchemaConnector implements SchemaConnector {

	private final static String RAW2NUM_FUNC = 
			"CREATE OR REPLACE FUNCTION raw2num(i_raw RAW) RETURN NUMBER " +
			"as " +
	    	"n NUMBER; " +
	    "begin " +
	    	"dbms_stats.convert_raw_value(i_raw, n); " +
	    	"return n; " +
	    "end;"; 
	
	private final static String RAW2DATE_FUNC = 
			"CREATE OR REPLACE FUNCTION raw2date(i_raw RAW) RETURN DATE " +
			"as " +
	    	"n DATE; " +
	    "begin " +
	    	"dbms_stats.convert_raw_value(i_raw, n); " +
	    	"return n; " +
	    "end;"; 
	
	private final static String RAW2CHAR_FUNC = 
			"CREATE OR REPLACE FUNCTION raw2char(i_raw RAW) RETURN VARCHAR2 " +
			"as " +
	    	"n VARCHAR2(1); " +
	    "begin " +
	    	"dbms_stats.convert_raw_value(i_raw, n); " +
	    	"return n; " +
	    "end;"; 

	
	private final JdbcConnector connector;
	
	private static final Logger LOGGER = Logger.getLogger(OracleSchemaConnector.class);
	
	 public OracleSchemaConnector(final JdbcConnector jdbcConnector) {
		 	this.connector = jdbcConnector;
		  LOGGER.info("entering OracleSchemaConnector() ...");  
		  try {	// register set of conversion functions 
		  	LOGGER.debug("register raw2* functions ...");
			  this.connector.execPreparedStmt(RAW2NUM_FUNC);
			  this.connector.execPreparedStmt(RAW2DATE_FUNC);
			  this.connector.execPreparedStmt(RAW2CHAR_FUNC);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		  LOGGER.info("leaving OracleSchemaConnector");  
	 }
	
	@Override
	public Set<Quartet<String, String, String, String>> getReferences(String schema) throws SQLException {
		return this.connector.getReferences(schema);
	}
	
	
}
