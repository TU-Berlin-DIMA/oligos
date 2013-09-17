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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class OracleColumnConnector<T> implements ColumnConnector<T> {
	
	
	// TODO make preparedStmt
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
	
	private final static String RAW2STR_FUNC = 
			"CREATE OR REPLACE FUNCTION raw2str(i_raw RAW) RETURN VARCHAR2 " +
			"as " +
	    	"n VARCHAR2; " +
	    "begin " +
	    	"dbms_stats.convert_raw_value(i_raw, n); " +
	    	"return n; " +
	    "end;"; 
	
	private static String DBMS_STATS_GATHER = 
			"begin "+
					"dbms_stats.gather_table_stats("+
						"ownname => '<schema>', " +
						"tabname => '<table>', " + 
						"estimate_percent => 100, " + 
						"method_opt => 'for columns <column> SIZE 20'" +
					"); " +
			"end;";
	
	//private static String DBMS_STATS_GATHER = 
	
	// TODO support don't suppress check constraints as soon as support by Framework
	private final static String CONSTRAINT_QUERY = 
			"SELECT constraint_type FROM ALL_CONSTRAINTS ac, ALL_CONS_COLUMNS cc " +
			"WHERE ac.constraint_name = cc.constraint_name " +
			"AND ac.owner = ? AND ac.table_name = ? " +
			"AND cc.column_name = ? AND ac.constraint_type != 'C'";

	private String DOMAIN_QUERY = 
			"SELECT raw2<T>(low_value) LOW_VALUE, raw2<T>(high_value) HIGH_VALUE, " +
			"num_nulls, num_distinct, nullable, data_type, data_length, data_scale " +
			"FROM ALL_TAB_COLUMNS WHERE owner = ? AND table_name = ? AND column_name = ?";

	private final static String MOST_FREQUENT_QUERY =
			"SELECT endpoint_value, endpoint_number - nvl(prev_number,0) frequency " +
			"FROM (" + 
			"SELECT endpoint_value, endpoint_number, lag(endpoint_number,1) over(order by endpoint_number) prev_number " +
			"FROM USER_TAB_HISTOGRAMS " +
			"WHERE table_name = ? AND column_name = ?" +
			") " + 
			"ORDER BY frequency DESC";
			
	private static String QUANTILE_HISTOGRAM_QUERY_DATE =
			"SELECT to_date(ENDPOINT_VALUE, 'J') as colvalue, round((NUM_ROWS*(Select CUME_DIST(to_date(endpoint_value, 'J')) " +
			"WITHIN GROUP (ORDER BY <column>) FROM <table>)),0) as cdf " +
			"FROM   DUAL, USER_HISTOGRAMS uh, USER_TAB_STATISTICS at " +
			"WHERE  uh.TABLE_NAME= ? " +
			"AND    uh.COLUMN_NAME= ? " +
			"AND    at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY cdf asc";
	
	
	private static String QUANTILE_HISTOGRAM_QUERY_NUMBER = 
			"SELECT ENDPOINT_VALUE as colvalue, round((NUM_ROWS*(Select CUME_DIST(endpoint_value) " + 
			"WITHIN GROUP (ORDER BY <column>) FROM <table>)),0) as cdf " + // insert w/o quotation marks
			"FROM   DUAL, USER_HISTOGRAMS uh, ALL_TAB_STATISTICS at " +
			"WHERE  uh.TABLE_NAME= ? " + // insert w/ quotation marks
			"AND    uh.COLUMN_NAME= ? " +
			"AND    at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY cdf ASC";
	
	
	private final JdbcConnector connector;
	private final String schema;
	private final String table;
	private final String column;
	private final Parser<T> parser;
	private final Class columnType;
	//private String DomainQueryString;
	private String QUANTILE_HISTOGRAM_QUERY;

	
	public OracleColumnConnector(final JdbcConnector jdbcConnector, final ColumnId columnId, final Parser<T> parser) {
		this(jdbcConnector, columnId.getSchema(), columnId.getTable(), columnId.getColumn(), columnId.getClass(), parser);
	}

	public OracleColumnConnector(final JdbcConnector jdbcConnector, final String schema
	      , final String table, final String column, final Class columnType,final Parser<T> parser) {
		
		this.connector = jdbcConnector;
	  this.schema = schema;
	  this.table = table;
	  this.column = column;
	  this.parser = parser;
	  this.columnType = columnType;
	  
	  // replace missing type information for appropriate conversion function call and histogram query
	  if (this.columnType.equals(Date.class)){
	  	this.DOMAIN_QUERY = this.DOMAIN_QUERY.replaceAll("<T>", "date");
	  	this.QUANTILE_HISTOGRAM_QUERY = OracleColumnConnector.QUANTILE_HISTOGRAM_QUERY_DATE;
	  	
	  }
	  else if (this.columnType.equals(BigDecimal.class)){
	  	this.DOMAIN_QUERY = this.DOMAIN_QUERY.replaceAll("<T>", "num");	
	  	this.QUANTILE_HISTOGRAM_QUERY = OracleColumnConnector.QUANTILE_HISTOGRAM_QUERY_NUMBER;
	  }
	  // TODO: support histogram queries for strings -> need for conversion
	  else if (this.columnType.equals(Character.class))
	  	this.DOMAIN_QUERY = this.DOMAIN_QUERY.replaceAll("<T>", "char");
	  else if (this.columnType.equals(String.class))
	  	this.DOMAIN_QUERY = this.DOMAIN_QUERY.replaceAll("<T>", "str");
	  else
	  	throw new Error("Unknown column type");
	  
	  // replace missing schema, table, and column values
	  OracleColumnConnector.DBMS_STATS_GATHER = 
	  		OracleColumnConnector.DBMS_STATS_GATHER.replaceAll("<schema>", this.schema).replaceAll("<table>", this.table).replaceAll("<column>", this.column);
	  this.QUANTILE_HISTOGRAM_QUERY = this.QUANTILE_HISTOGRAM_QUERY.replaceAll("<table>", this.table).replaceAll("<column>", this.column);
	  
	  
	  
	  // register set of conversion functions 
	  // TODO: move this up in call hierarchy
	  try {
	  	this.connector.execPreparedStmt(RAW2NUM_FUNC);
	  	this.connector.execPreparedStmt(RAW2DATE_FUNC);
	  	this.connector.execPreparedStmt(RAW2CHAR_FUNC);
	  	this.connector.execPreparedStmt(RAW2STR_FUNC);	
	  	// gather statistics
	  	this.connector.execPreparedStmt(OracleColumnConnector.DBMS_STATS_GATHER);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<Constraint> getConstraints() throws SQLException {
		System.out.println("entering OracleColumnConnector:getConstraints");
		Set<Constraint> constraints = Sets.newHashSet();
		String con = this.connector.scalarQuery(CONSTRAINT_QUERY, "type", this.schema, this.table, this.column);
		System.out.println("constraints = " + con);
		if (con != null) {
			if (con.equals("U")) {
				constraints.add(Constraint.UNIQUE);
		    } 
			else if (con.equals("P")) {
				constraints.add(Constraint.PRIMARY_KEY);
		    } 
			else if (con.equals("R")) {
		        constraints.add(Constraint.FOREIGN_KEY);
		    }
		}
		System.out.println("leaving OracleColumnConnector:getConstraints");
		return constraints;		
	}

	
	@Override
	public long getNumNulls() throws SQLException {
		System.out.println("entering OracleColumnConnector:getNumNulls");
		long ret = this.connector.<BigDecimal>scalarQuery(DOMAIN_QUERY, "num_nulls", this.schema, this.table, this.column).longValue();
		System.out.println("leaving OracleColumnConnector:getNumNulls = " + ret);
		return ret;
	}

	@Override
	public long getCardinality() throws SQLException {
		System.out.println("entering OracleColumnConnector:getCardinality");
		long ret = this.connector.<BigDecimal>scalarQuery(DOMAIN_QUERY, "num_distinct", this.schema, this.table, this.column).longValue();
		System.out.println("leaving OracleColumnConnector:getNumNulls = " + ret);
		return ret;
	}
	 
	@Override
	public T getMin() throws SQLException {
		System.out.println("entering OracleColumnConnector:getMin");
		T minT = this.connector.scalarQuery(this.DOMAIN_QUERY, "low_value", this.schema, this.table, this.column);
		System.out.println("... executed scalarQuery, min = " + minT + "\ttable = " + this.table +", col = " + this.column);
		return (T) minT; //this.parser.fromString(minStr);
	}

	@Override
	public T getMax() throws SQLException {
		T maxT = this.connector.scalarQuery(this.DOMAIN_QUERY, "high_value", this.schema, this.table, this.column);
		System.out.println("leaving OracleColumnConnector:getMax, max = " + maxT + "\ttable = " + this.table +", col = " + this.column);
		return (T) maxT;
	}

	@Override
	public Map<T, Long> getMostFrequentValues() throws SQLException {
		System.out.println("entering OracleColumnConnector:getMostFreqVals ...");
		Map<T, Long> ret = this.connector.histogramQuery(MOST_FREQUENT_QUERY, "endpoint_value", "frequency", this.parser, this.table, this.column);
		System.out.println("leaving OracleColumnConnector:getMostFreqVals");
		return ret;
	}
	
	@Override
	public Map<T, Long> getHistogram() throws SQLException {
		System.out.println("entering OracleColumnConnector:getHistogram ...");
		Map<T, Long> ret = this.connector.histogramQuery(QUANTILE_HISTOGRAM_QUERY, "colvalue", "cdf", this.parser, this.table, this.column );
		System.out.println("leaving OracleColumnConnector:getMostFreqVals");
		
		return ret;
	}

}
