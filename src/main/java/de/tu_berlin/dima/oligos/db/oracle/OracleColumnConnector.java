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
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

import org.apache.log4j.Logger;

public class OracleColumnConnector<T> implements ColumnConnector<T> {
	
	
	private String RAW2STR_FUNC = 
			"CREATE OR REPLACE FUNCTION raw2str(i_raw RAW) RETURN VARCHAR2 " +
			"as " +
	    	"n VARCHAR2(<length>); " +
	    "begin " +
	    	"dbms_stats.convert_raw_value(i_raw, n); " +
	    	"return n; " +
	    "end;"; 
	
	private String DBMS_STATS_GATHER = 
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
			"SELECT constraint_type as type FROM ALL_CONSTRAINTS ac, ALL_CONS_COLUMNS cc " +
			"WHERE ac.constraint_name = cc.constraint_name " +
			"AND ac.owner = ? AND ac.table_name = ? " +
			"AND cc.column_name = ? AND ac.constraint_type != 'C'";

	private String DOMAIN_QUERY = 
			"SELECT raw2<T>(low_value) LOW_VALUE, raw2<T>(high_value) HIGH_VALUE, " +
			"num_nulls, num_distinct, nullable, data_type, data_length, data_scale " +
			"FROM ALL_TAB_COLUMNS WHERE owner = ? AND table_name = ? AND column_name = ?";

	// TODO: does not work for strings
	private final static String MOST_FREQUENT_QUERY_GENERIC =
			"SELECT endpoint_value, endpoint_number - nvl(prev_number,0) frequency " +
			"FROM (" + 
			"SELECT endpoint_value, endpoint_number, lag(endpoint_number,1) over(order by endpoint_number) prev_number " +
			"FROM USER_TAB_HISTOGRAMS " +
			"WHERE table_name = ? AND column_name = ?" +
			") " + 
			"ORDER BY frequency DESC";
	
	private final static String MOST_FREQUENT_QUERY_STRING = 
			"SELECT endpoint_actual_value as endpoint_value, endpoint_number - nvl(prev_number,0) frequency "+
			"FROM (SELECT endpoint_actual_value, endpoint_number, lag(endpoint_number,1) over(order by endpoint_number) prev_number "+
			"FROM USER_TAB_HISTOGRAMS "+
			"WHERE table_name = ? AND column_name = ?) "+
			"ORDER BY frequency DESC";
			
	private static String QUANTILE_HISTOGRAM_QUERY_DATE =
			"SELECT to_date(ENDPOINT_VALUE, 'J') as colvalue, round((NUM_ROWS*(Select CUME_DIST(to_date(endpoint_value, 'J')) " +
			"WITHIN GROUP (ORDER BY <column>) FROM <table>)),0) as cdf " +
			"FROM   DUAL, USER_HISTOGRAMS uh, USER_TAB_STATISTICS at " +
			"WHERE  uh.TABLE_NAME= ? " +
			"AND    uh.COLUMN_NAME= ? " +
			"AND    at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY cdf asc";
	
	/**
	 * Query string for numbers and strings that are unique in the first ~6 Bytes
	 */
	private static String QUANTILE_HISTOGRAM_QUERY_NUMBER = 
			"SELECT ENDPOINT_VALUE as colvalue, round((NUM_ROWS*(Select CUME_DIST(endpoint_value) " + 
			"WITHIN GROUP (ORDER BY <column>) FROM <table>)),0) as cdf " + // insert w/o quotation marks
			"FROM   DUAL, USER_HISTOGRAMS uh, ALL_TAB_STATISTICS at " +
			"WHERE  uh.TABLE_NAME= ? " + // insert w/ quotation marks
			"AND    uh.COLUMN_NAME= ? " +
			"AND    at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY cdf ASC";
	
	/**
	 * Query string for string values that have duplicates if truncated after 15 digits. Their real (unconverted) value 
	 * is stored in endpoint_actual_value. 
	 */
	private static String QUANTILE_HISTOGRAM_QUERY_STRING = 
			"SELECT ENDPOINT_ACTUAL_VALUE as colvalue, round((NUM_ROWS*(Select CUME_DIST(endpoint_actual_value) "+
			"WITHIN GROUP (ORDER BY O_CLERK) FROM H_ORDER)),0) as cdf "+
			"FROM DUAL, USER_HISTOGRAMS uh, ALL_TAB_STATISTICS at "+
			"WHERE  uh.TABLE_NAME= ? " +
			"AND uh.COLUMN_NAME= ? " + 
			"AND at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY cdf ASC";
	/**
	 * Query for presence of endpoint actual value
	 */
	private String ENDPOINT_ACTUAL_VALUE = 
			"SELECT ENDPOINT_ACTUAL_VALUE as colvalue "+
			"FROM USER_HISTOGRAMS " +
			"WHERE TABLE_NAME = ? AND COLUMN_NAME = ? "+
			"AND ROWNUM <= 1";

	private final JdbcConnector connector;
	private final String schema;
	private final String table;
	private final String column;
	private final Parser<T> parser;
	private final Class<?> columnType;
	private final TypeInfo type;
	private String QUANTILE_HISTOGRAM_QUERY;
	private String MOST_FREQUENT_QUERY;
	private boolean isEndpointActualValue = false; // indicates whether endpoint_actual_value in histogram view is filled
  private static final Logger LOGGER = Logger.getLogger(OracleColumnConnector.class);
	
	public OracleColumnConnector(final JdbcConnector jdbcConnector, final ColumnId columnId, final Parser<T> parser) {
		this(jdbcConnector, columnId.getSchema(), columnId.getTable(), columnId.getColumn(), columnId.getClass(), parser);
	}
	
	// deprecated -> need typeInfo Object
	public OracleColumnConnector(final JdbcConnector jdbcConnector, final String schema
      , final String table, final String column, final Class<?> columnType,final Parser<T> parser) {
		this(jdbcConnector, schema, table, column, columnType, parser, null);
	}
	
	public OracleColumnConnector(final JdbcConnector jdbcConnector, final String schema
      , final String table, final String column, final Class<?> columnType,final Parser<T> parser, TypeInfo type){
		LOGGER.debug("entering OracleColumnConnector() ...");
		this.connector = jdbcConnector;
	  this.schema = schema;
	  this.table = table;
	  this.column = column;
	  this.parser = parser;
	  this.columnType = columnType;
	  this.type = type;
	  
	
	  
	  
	  // replace missing type information for appropriate conversion function call and histogram query
	  if (!setDomainQueryString())
	  	;
	  setHistogramQueryString();
	  setGatherStatisticsString();
	  
  	// create statistical view
	  gatherStatistics();
	  
  	LOGGER.debug("leaving OracleColumnConnector()");
	}

	

	/**
	 * Create view for Oracle's optimizer statistics.
	 * 
	 * @param 	void
	 * @return 	void
	 */
	private void gatherStatistics(){
		try {	
	  	this.connector.execPreparedStmt(this.DBMS_STATS_GATHER);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Set missing values for schema, table, and column for gathering statistics.
	 * 
	 *  @param 	void
	 *  @return void
	 */
	private void setGatherStatisticsString() {
		// replace missing schema, table, and column values
	  this.DBMS_STATS_GATHER = 
	  		this.DBMS_STATS_GATHER.replaceAll("<schema>", this.schema).replaceAll("<table>", this.table).replaceAll("<column>", this.column);
	}

	/**
	 * Setter for appropriate conversion function call in SQL.
	 * 
	 * @param 	void
	 * @return  void
	 */
	private boolean setDomainQueryString(){
		if (this.columnType.equals(Date.class))
	  	this.DOMAIN_QUERY = this.DOMAIN_QUERY.replaceAll("<T>", "date");
	  else if (this.columnType.equals(BigDecimal.class))
	  	this.DOMAIN_QUERY = this.DOMAIN_QUERY.replaceAll("<T>", "num");	
	  else if (this.columnType.equals(String.class))
	  	this.DOMAIN_QUERY = this.DOMAIN_QUERY.replaceAll("<T>", "str");
	  else if (this.columnType.equals(Character.class))
	  	this.DOMAIN_QUERY = this.DOMAIN_QUERY.replaceAll("<T>", "str");
	  else{
	  	LOGGER.error("Unsupported column type: " + this.columnType.toString() + " for column '" + this.column + "' in table '" + this.table +"'");
//	  	throw new Error("Unknown column type: " + this.columnType.toString());
	  	return false;
	  }
		return true;
	}
	
	/**
	 * Set SQL histogram query string according to column type.
	 * 
	 * @param  void
	 * @return void
	 * @throws SQLException 
	 */
	private void setHistogramQueryString(){
		if (this.columnType.equals(Date.class))
	  	this.QUANTILE_HISTOGRAM_QUERY = OracleColumnConnector.QUANTILE_HISTOGRAM_QUERY_DATE;
	  else if (this.columnType.equals(BigDecimal.class))
	  	this.QUANTILE_HISTOGRAM_QUERY = OracleColumnConnector.QUANTILE_HISTOGRAM_QUERY_NUMBER;
	  else if (this.columnType.equals(String.class)){
	  	try {// if endpoint_actual_value present, read it out (return type is string)
				T res = this.connector.scalarQuery(this.ENDPOINT_ACTUAL_VALUE, "colvalue", this.table, this.column);
				if (res != null)
					this.isEndpointActualValue = true;
				this.QUANTILE_HISTOGRAM_QUERY = (this.isEndpointActualValue)? OracleColumnConnector.QUANTILE_HISTOGRAM_QUERY_STRING : OracleColumnConnector.QUANTILE_HISTOGRAM_QUERY_NUMBER; 
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(-1);
			}
	  }
	  else if (this.columnType.equals(Character.class))
	  	this.QUANTILE_HISTOGRAM_QUERY = OracleColumnConnector.QUANTILE_HISTOGRAM_QUERY_NUMBER;
	  else
	  	throw new Error("Unknown column type");
		this.MOST_FREQUENT_QUERY = (this.isEndpointActualValue) ? OracleColumnConnector.MOST_FREQUENT_QUERY_STRING : OracleColumnConnector.MOST_FREQUENT_QUERY_GENERIC;
		this.QUANTILE_HISTOGRAM_QUERY = this.QUANTILE_HISTOGRAM_QUERY.replaceAll("<table>", this.table).replaceAll("<column>", this.column);
	}
	
	
	/**
	 * Query constraint type(s) for this column.
	 * 
	 * @param 	void
	 * @return 	Set
	 */
	@Override
	public Set<Constraint> getConstraints() throws SQLException {
		LOGGER.debug("entering OracleColumnConnector:getConstraints");
		Set<Constraint> constraints = Sets.newHashSet();
		String con = this.connector.scalarQuery(CONSTRAINT_QUERY, "type", this.schema, this.table, this.column);
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
		LOGGER.debug("leaving OracleColumnConnector:getConstraints");
		return constraints;		
	}

	// TODO: one DOMAIN_QUERY to get all subsequent values
	@Override
	public long getNumNulls() throws SQLException {
		LOGGER.debug("entering OracleColumnConnector:getNumNulls ...");
		register_raw2str_func();
		long ret = this.connector.<BigDecimal>scalarQuery(DOMAIN_QUERY, "num_nulls", this.schema, this.table, this.column).longValue();
		LOGGER.debug("leaving OracleColumnConnector:getNumNulls");
		return ret;
	}

	@Override
	public long getCardinality() throws SQLException {
		LOGGER.debug("entering OracleColumnConnector:getCardinality ...");
		register_raw2str_func();
		long ret = this.connector.<BigDecimal>scalarQuery(DOMAIN_QUERY, "num_distinct", this.schema, this.table, this.column).longValue();
		LOGGER.debug("leaving OracleColumnConnector:getCardinality");
		return ret;
	}

	// TODO convert if columnType is String and query returns number
	@Override
	public T getMin() throws SQLException {
		LOGGER.debug("entering OracleColumnConnector:getMin ...");
		LOGGER.debug("column = " + this.column);
		register_raw2str_func();
		T minT = this.connector.scalarQuery(this.DOMAIN_QUERY, "low_value", this.schema, this.table, this.column);
		if (this.columnType.equals(String.class))
			minT = this.parser.fromString(((String) minT).replaceAll("'", ""));
		LOGGER.debug("leaving OracleColumnConnector:getMin");
		return (T) minT; //this.parser.fromString(minStr);
	}

	// TODO convert if columnType is String and query returns number
	@Override
	public T getMax() throws SQLException {
		LOGGER.debug("entering OracleColumnConnector:getMin ...");
		register_raw2str_func();
		T maxT = this.connector.scalarQuery(this.DOMAIN_QUERY, "high_value", this.schema, this.table, this.column);
		LOGGER.debug("leaving OracleColumnConnector:getMax");
		return (T) maxT;
	}

	@Override
	public Map<T, Long> getMostFrequentValues() throws SQLException {
		LOGGER.debug("entering OracleColumnConnector:getMostFreqVals ...");
		Map<T, Long> ret = this.connector.histogramQuery(this.MOST_FREQUENT_QUERY, "endpoint_value", "frequency", this.parser, this.table, this.column);
		// convert keys into strings if endpoint_actual_value was empty  
		if (!this.isEndpointActualValue && this.columnType.equals(String.class)){
			Map<String, Long> ret_new = Maps.newLinkedHashMap();
			for (Map.Entry<T, Long> entry: ret.entrySet()){
				ret_new.put(oraNum2String(new BigDecimal((String)entry.getKey())), entry.getValue());
			}
			return (Map<T, Long>) ret_new;
		}
		LOGGER.debug("leaving OracleColumnConnector:getMostFreqVals");
		return ret;
	}
	
	@Override
	public Map<T, Long> getHistogram() throws SQLException {
		LOGGER.debug("entering OracleColumnConnector:getHistogram ...");
		Map<T, Long> ret = this.connector.histogramQuery(QUANTILE_HISTOGRAM_QUERY, "colvalue", "cdf", this.parser, this.table, this.column );
		
		// test whether output has to be converted
		if (!this.isEndpointActualValue && this.columnType.equals(String.class) || this.columnType.equals(Character.class))
		{
			Map<String, Long> ret_new = Maps.newLinkedHashMap();
			for (Map.Entry<T, Long> entry: ret.entrySet()){
				ret_new.put(oraNum2String(new BigDecimal((String)entry.getKey())), entry.getValue());
		//		System.out.println("ret_new[" + oraNum2String(new BigDecimal((String)entry.getKey())) + "] = " + entry.getValue());
			}
			return (Map<T, Long>) ret_new;
		}
		//for (Map.Entry<T, Long> entry :ret.entrySet())
		//	System.out.println("hist[" + entry.getKey().toString() + "] = " + entry.getValue().toString());
		
		LOGGER.debug("leaving OracleColumnConnector:getHistogram");
		return ret;
	}
	/**
	 * Converts Oracle's numerical endpoint_values for strings back into a string. Note, that only the first 6 Bytes are recovered. 
	 * Missing trailing characters are filled with blanks. 
	 * @param e 	Oracle's bin value representing a string
	 * @return 		recovered string
	 */
	private String oraNum2String(BigDecimal val){
		char s[] = new char[this.type.getLength()];
		int nl;
		BigDecimal divisor;
		BigDecimal C256 = BigDecimal.valueOf(256);
		int exactDigits = 6; 
		// compute all reconstructable characters
		for (int i = 1; i <= Math.min(exactDigits, this.type.getLength()); i++){
			divisor = C256.pow(15-i);
			nl = val.divide(divisor).intValue();
			if (nl == 0){
				exactDigits = i-1;
				break;
			}
			val = val.subtract((new BigDecimal(nl)).multiply(divisor));
			s[i-1] = (char) nl;
		}
		// pad rest with blanks
		for (int i = exactDigits; i < this.type.getLength(); i++)
			s[i] = (char) 32;
		return new String(s);
	}
	
	/**
	 * Register right before usage raw2str conversion function with appropriate buffer size to avoid PL/SQL buffer size errors.
	 */
	private void register_raw2str_func(){
	  if (this.columnType.equals(String.class) || this.columnType.equals(Character.class)){
	  	if (this.columnType.equals(String.class))
	  		this.RAW2STR_FUNC = this.RAW2STR_FUNC.replaceAll("<length>", Integer.toString(type.getLength()));
	  	else if (this.columnType.equals(Character.class))
	  		this.RAW2STR_FUNC = this.RAW2STR_FUNC.replaceAll("<length>", Integer.toString(1));
	  	try {
	  		//System.out.println("exec raw2str_func = " + this.RAW2STR_FUNC);
	  		this.connector.execPreparedStmt(RAW2STR_FUNC);
	  	} catch (SQLException e) {
	  		e.printStackTrace();
	  		System.exit(-1);
	  	}		
	  }
	}

}
