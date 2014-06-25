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
						"method_opt => 'for columns <column> SIZE 10'" +
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

	/**
	 * Most frequent values for type in {number, string}
	 */
	private String MOST_FREQUENT_QUERY_GENERIC =
			"SELECT endpoint_value, endpoint_number - nvl(prev_number,0) frequency " +
			"FROM (" + 
			"SELECT endpoint_value, endpoint_number, lag(endpoint_number,1) over(order by endpoint_number) prev_number " +
			"FROM USER_TAB_HISTOGRAMS " +
			"WHERE table_name = ? AND column_name = ?" +
			") " + 
			"ORDER BY frequency DESC";
	
	/**
	 * Most frequent values for date type
	 */
	private String MOST_FREQUENT_QUERY_DATE =
			"SELECT to_date(endpoint_value, 'J'), endpoint_number - nvl(prev_number,0) frequency " +
			"FROM (" + 
			"SELECT endpoint_value, endpoint_number, lag(endpoint_number,1) over(order by endpoint_number) prev_number " +
			"FROM USER_TAB_HISTOGRAMS " +
			"WHERE table_name = ? AND column_name = ?" +
			") " + 
			"ORDER BY frequency DESC";
	
	
	
	/**
	 *  Most frequent values for strings using the endpoint_actual_value field when strings do not differ in ~ first 6 Bytes
	 */
	private String MOST_FREQUENT_QUERY_STRING = 
			"SELECT endpoint_actual_value as endpoint_value, endpoint_number - nvl(prev_number,0) frequency "+
			"FROM (SELECT endpoint_actual_value, endpoint_number, lag(endpoint_number,1) over(order by endpoint_number) prev_number "+
			"FROM USER_TAB_HISTOGRAMS "+
			"WHERE table_name = ? AND column_name = ?) "+
			"ORDER BY frequency DESC";
		
	/**
	 *  Oracle's dictionary either provides a height-balanced or a frequency histogram 
	 * depending on whether number of buckets falls below the number of distinct values 
	 * */
	
	/**
	 *  cdf query for height-balanced histograms on numbers and strings
	 */
	private String HISTOGRAM_HEIGHT_BALANCED_NUMBER = 
			"SELECT ENDPOINT_VALUE as colvalue, endpoint_number*num_rows/num_buckets as cdf " +
			"FROM USER_HISTOGRAMS uh, ALL_TAB_STATISTICS at, ALL_TAB_COL_STATISTICS atc " +
			"WHERE  atc.OWNER = ? AND atc.TABLE_NAME = ? AND atc.COLUMN_NAME = ? " + 
			"AND uh.TABLE_NAME= atc.TABLE_NAME " +
			"AND uh.COLUMN_NAME= atc.COLUMN_NAME " +
			"AND at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY endpoint_number ASC";

  /**
   * cdf query for height-balanced histograms on date type
   */
	private String HISTOGRAM_HEIGHT_BALANCED_DATE = 
			"SELECT to_date(ENDPOINT_VALUE, 'J') as colvalue, endpoint_number*num_rows/num_buckets as cdf " +
			"FROM USER_HISTOGRAMS uh, USER_TAB_STATISTICS at, ALL_TAB_COL_STATISTICS atc " +
			"WHERE  atc.OWNER = ? AND atc.TABLE_NAME = ? AND atc.COLUMN_NAME = ? " + 
			"AND uh.TABLE_NAME= atc.TABLE_NAME " +
			"AND uh.COLUMN_NAME= atc.COLUMN_NAME " +
			"AND at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY endpoint_number ASC";
	
	/**
	 * cdf query for frequency histograms
	 */
	private String HISTOGRAM_FREQ_DATE =
			"SELECT to_date(ENDPOINT_VALUE, 'J') as colvalue, ENDPOINT_NUMBER as cdf " +
			"FROM ALL_TAB_HISTOGRAMS " +
			"WHERE owner = ? AND table_name = ? and column_name = ? " +
			"ORDER BY endpoint_number ASC";
	
		/*	"SELECT to_date(ENDPOINT_VALUE, 'J') as colvalue, round((NUM_ROWS*(Select CUME_DIST(to_date(endpoint_value, 'J')) " +
			"WITHIN GROUP (ORDER BY <column>) FROM <table>)),0) as cdf " +
			"FROM   DUAL, USER_HISTOGRAMS uh, USER_TAB_STATISTICS at " +
			"WHERE  uh.TABLE_NAME= ? " +
			"AND    uh.COLUMN_NAME= ? " +
			"AND    at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY cdf asc";
	*/
	/**
	 * Query string for numbers and strings that are unique in the first ~6 Bytes
	 */
	// begin dbms_stats.gather_table_stats(ownname => 'tpch', tabname => 'h_order', estimate_percent => 100, method_opt => 'for columns o_orderstatus SIZE 10'); end;
	//SELECT ENDPOINT_VALUE as colvalue, ENDPOINT_NUMBER as cdf, table_name as tab, column_name as col FROM ALL_TAB_HISTOGRAMS WHERE owner = 'TPCH' AND table_name = 'h_order' and column_name = 'o_orderstatus'
	//SELECT ENDPOINT_VALUE as colvalue, ENDPOINT_NUMBER as cdf FROM ALL_TAB_HISTOGRAMS WHERE owner = 'TPCH' AND table_name = 'H_ORDER' and column_name = 'O_ORDERSTATUS';
	private String HISTOGRAM_FREQ_NUMBER = 
			"SELECT ENDPOINT_VALUE as colvalue, ENDPOINT_NUMBER as cdf " +
			"FROM ALL_TAB_HISTOGRAMS " +
			"WHERE owner = ? AND table_name = ? and column_name = ? " +
			"ORDER BY endpoint_number ASC";
	
	/*private String QUANTILE_HISTOGRAM_QUERY_NUMBER = 
			"SELECT ENDPOINT_VALUE as colvalue, round((NUM_ROWS*(Select CUME_DIST(endpoint_value) " + 
			"WITHIN GROUP (ORDER BY <column>) FROM <table>)),0) as cdf " + // insert w/o quotation marks
			"FROM   DUAL, USER_HISTOGRAMS uh, ALL_TAB_STATISTICS at " +
			"WHERE  uh.TABLE_NAME= ? " + // insert w/ quotation marks
			"AND    uh.COLUMN_NAME= ? " +
			"AND    at.TABLE_NAME= uh.TABLE_NAME " +
			"ORDER BY cdf ASC";*/
	
	/**
	 * Query for presence of endpoint actual value
	 */
	private String ENDPOINT_ACTUAL_VALUE = 
			"SELECT ENDPOINT_ACTUAL_VALUE as colvalue "+
			"FROM USER_HISTOGRAMS " +
			"WHERE TABLE_NAME = ? AND COLUMN_NAME = ? "+
			"AND ROWNUM <= 1";
	
	private String HISTOGRAM_TYPE = 
			"SELECT HISTOGRAM FROM ALL_TAB_COL_STATISTICS " +
			"WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";

	private final JdbcConnector connector;
	private final String schema;
	private final String table;
	private final String column;
	private final Parser<T> parser;
	private final Class<?> columnType;
	private final TypeInfo type;
	private String HISTOGRAM_QUERY;
	private String MOST_FREQUENT_QUERY;
	private boolean isEndpointActualValue = false; // indicate whether endpoint_actual_value in histogram view is filled
	private boolean isHeightBalanced = false; // indicates whether histogram from ALL_TAB_HISTOGRAM view is a height balanced (else frequency or none)
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
	  	LOGGER.error("omit column '" + this.column + "' in '"+this.table+ "' due to unsupported column type: " + this.columnType.getName());;
	  setHistogramAndMostFrequentQueryString();
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
	private void setHistogramAndMostFrequentQueryString(){
		LOGGER.trace("Histogram check");
		// check for histogram type height balanced, frequency, or none
		try {
			//System.out.println("Histogram type query string = " + this.HISTOGRAM_TYPE);
			T histogram = this.connector.scalarQuery(this.HISTOGRAM_TYPE, "histogram", this.table, this.column);
			if (histogram.equals("HEIGHT BALANCED"))
				this.isHeightBalanced = true;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		// set to date query
		if (this.columnType.equals(Date.class))
	  	this.HISTOGRAM_QUERY = (this.isHeightBalanced) ? this.HISTOGRAM_HEIGHT_BALANCED_DATE : this.HISTOGRAM_FREQ_DATE;
		// set to number/string query
	  else if (this.columnType.equals(BigDecimal.class))
	  	this.HISTOGRAM_QUERY = (this.isHeightBalanced) ? this.HISTOGRAM_HEIGHT_BALANCED_NUMBER : this.HISTOGRAM_FREQ_NUMBER;
	  // set to string query
	  else if (this.columnType.equals(String.class)){
	  	try {// if endpoint_actual_value present, read it out (return type is string)
				T res = this.connector.scalarQuery(this.ENDPOINT_ACTUAL_VALUE, "colvalue", this.table, this.column);
				if (res != null)
					this.isEndpointActualValue = true;
				// read endpoint_actual_value column from ALL_TAB_HISTOGRAMS
				if (this.isEndpointActualValue){
					this.HISTOGRAM_QUERY = (this.isHeightBalanced) ? 
							this.HISTOGRAM_HEIGHT_BALANCED_NUMBER.replaceFirst("endpoint_value", "endpoint_actual_value") : 
								this.HISTOGRAM_FREQ_NUMBER.replaceAll("endpoint_value", "endpoint_actual_value");
				}
				// else read endpoint_value column from ALL_TAB_HISTOGRAMS
				else
						this.HISTOGRAM_QUERY = (this.isHeightBalanced) ? this.HISTOGRAM_HEIGHT_BALANCED_NUMBER : this.HISTOGRAM_FREQ_NUMBER;
				
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(-1);
			}
	  }
	  else
	  	throw new Error("Unknown column type");
		this.MOST_FREQUENT_QUERY = (this.isEndpointActualValue) ? this.MOST_FREQUENT_QUERY_STRING : this.MOST_FREQUENT_QUERY_GENERIC;
		this.MOST_FREQUENT_QUERY = (this.columnType.equals(Date.class)) ? this.MOST_FREQUENT_QUERY_DATE : this.MOST_FREQUENT_QUERY;
		this.HISTOGRAM_QUERY = this.HISTOGRAM_QUERY.replaceAll("<table>", this.table).replaceAll("<column>", this.column);
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
		if (!this.isEndpointActualValue && (this.columnType.equals(String.class))){
			Map<String, Long> ret_new = Maps.newLinkedHashMap();
			for (Map.Entry<T, Long> entry: ret.entrySet()){
				ret_new.put(oraNum2String(new BigDecimal((String)entry.getKey())), entry.getValue());
			}
			return (Map<T, Long>) ret_new;
		}
		LOGGER.debug("leaving OracleColumnConnector:getMostFreqVals");
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<T, Long> getHistogram() throws SQLException {
		LOGGER.debug("entering OracleColumnConnector:getHistogram ...");
		Map<T, Long> ret = this.connector.histogramQuery(HISTOGRAM_QUERY, "colvalue", "cdf", this.parser, this.schema, this.table, this.column);
				//this.connector.histogramQuery(HISTOGRAM_QUERY, "colvalue", "cdf", this.parser, this.table, this.column);
		
		// test whether output has to be converted
		if (!this.isEndpointActualValue && this.columnType.equals(String.class)){
			Map<String, Long> ret_new = Maps.newLinkedHashMap();
			for (Map.Entry<T, Long> entry: ret.entrySet()){
				ret_new.put(oraNum2String(new BigDecimal((String)entry.getKey())), entry.getValue());
			}
			return (Map<T, Long>) ret_new;
		}
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
	  if (this.columnType.equals(String.class)){
	  	this.RAW2STR_FUNC = this.RAW2STR_FUNC.replaceAll("<length>", Integer.toString(type.getLength()));
	  	try {
	  		this.connector.execPreparedStmt(RAW2STR_FUNC);
	  	} catch (SQLException e) {
	  		e.printStackTrace();
	  		System.exit(-1);
	  	}		
	  }
	}

}
