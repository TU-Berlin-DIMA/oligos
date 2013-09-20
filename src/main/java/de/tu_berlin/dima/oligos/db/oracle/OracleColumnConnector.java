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
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class OracleColumnConnector<T> implements ColumnConnector<T> {
	
	
	private final static String RAW2NUM_FUNC = 
			"CREATE OR REPLACE FUNCTION raw2num(rawval IN RAW) RETURN NUMBER AUTHID DEFINER IS " +
			"n NUMBER; " + 
			"BEGIN " +
			"dbms_stats.convert_raw_value(rawval, n);" +
			"RETURN n;" +
			"END raw2num;" +
			"/ ";
	
	private final static String CONSTRAINT_QUERY = 
			"SELECT constraint_type FROM ALL_CONSTRAINTS ac, ALL_CONS_COLUMNS cc " +
			"WHERE ac.constraint_name = cc.constraint_name " +
			"AND ac.owner = ? AND ac.table_name = ? " +
			"AND cc.column_name = ? AND ac.constraint_type != 'C'";

	private final static String DOMAIN_QUERY = 
			"SELECT raw2num(low_value) LOW_VALUE, raw2num(high_value) HIGH_VALUE, " +
			"num_nulls, num_distinct, nullable, data_type, data_length, data_scale " +
			"FROM ALL_TAB_COLUMNS WHERE owner = ? AND table_name = ? AND column_name = ?";
	
	private final static String DBMS_STATS_GATHER = "begin "+
			"dbms_stats.gather_table_stats(" +
			"ownname      => ?, " +
			"tabname      => ?, " + 
			"estimate_percent => 100, " + 
			"method_opt   => 'for columns ?' " +
			"); " +
			"end; " +
			"/";

	private final static String MOST_FREQUENT_QUERY =
			"SELECT endpoint_value, endpoint_number - nvl(prev_number,0) frequency " +
			"FROM (" + 
			"SELECT endpoint_value, lag(endpoint_number,1) over(order by endpoint_number) prev_number " +
			"FROM USER_TAB_HISTOGRAMS " +
			"WHERE table_name = ? AND column_name = 'P_SIZE'" +
			") " + 
			"ORDER BY frequency DESC";
	
	private final static String QUANTILE_HISTOGRAM_QUERY = 
			"SELECT endpoint_value, endpoint_number - nvl(prev_number,0) frequency " +
			"FROM (" + 
			"SELECT endpoint_value, lag(endpoint_number,1) over(order by endpoint_number) prev_number " +
			"FROM USER_TAB_HISTOGRAMS " +
			"WHERE table_name = ? AND column_name = 'P_SIZE'" +
			")";
			

	private final JdbcConnector connector;
	private final String schema;
	private final String table;
	private final String column;
	private final Parser<T> parser;
	  
	public OracleColumnConnector(final JdbcConnector jdbcConnector, final ColumnId columnId, final Parser<T> parser) {
		this(jdbcConnector, columnId.getSchema(), columnId.getTable(), columnId.getColumn(), parser);
	}

	public OracleColumnConnector(final JdbcConnector jdbcConnector, final String schema
	      , final String table, final String column, final Parser<T> parser) {
		this.connector = jdbcConnector;
	    this.schema = schema;
	    this.table = table;
	    this.column = column;
	    this.parser = parser;
	}

	@Override
	public Set<Constraint> getConstraints() throws SQLException {
		System.out.println("entering OracleColumnConnector:getConstraints");
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
		System.out.println("leaving OracleColumnConnector:getConstraints");
		return constraints;		
	}

	
	@Override
	public long getNumNulls() throws SQLException {
		System.out.println("entering OracleColumnConnector:getNumNulls");
		this.connector.execSQLCommand(RAW2NUM_FUNC, (Object) null);
		System.out.println("leaving OracleColumnConnector:getNumNulls");
		return this.connector.<Long>scalarQuery(DOMAIN_QUERY, "num_nulls", this.schema, this.table, this.column);
	}

	@Override
	public long getCardinality() throws SQLException {
		System.out.println("entering OracleColumnConnector:getCardinality");
		this.connector.execSQLCommand(RAW2NUM_FUNC, (Object) null);
		long ret = this.connector.<Long>scalarQuery(DOMAIN_QUERY, "num_distinct", this.schema, this.table, this.column);
		System.out.println("leaving OracleColumnConnector:getNumNulls");
		return ret;
	}
	
	@Override
	public T getMin() throws SQLException {
		System.out.println("entering OracleColumnConnector:getMin");
		this.connector.execSQLCommand(RAW2NUM_FUNC, (Object) null);
		String minStr = this.connector.scalarQuery(DOMAIN_QUERY, "low_value", this.schema, this.table, this.column);
		System.out.println("leaving OracleColumnConnector:getMin");
		return this.parser.fromString(minStr);
	}

	@Override
	public T getMax() throws SQLException {
		this.connector.execSQLCommand(RAW2NUM_FUNC, (Object) null);
		String minStr = this.connector.scalarQuery(DOMAIN_QUERY, "high_value", this.schema, this.table, this.column);
	    return this.parser.fromString(minStr);
	}

	@Override
	public Map<T, Long> getMostFrequentValues() throws SQLException {
		this.connector.execSQLCommand(DBMS_STATS_GATHER, schema, table, column);
		return this.connector.histogramQuery(MOST_FREQUENT_QUERY, "endpoint_value", "frequency", this.parser, this.schema, 
				this.table, this.column);
	}
	
	@Override
	public Map<T, Long> getHistogram() throws SQLException {
		this.connector.execSQLCommand(DBMS_STATS_GATHER, this.schema, this.table, this.column);
		return this.connector.histogramQuery(QUANTILE_HISTOGRAM_QUERY, "endpoint_value", "frequency", 
				this.parser, this.schema, this.table, this.column);
	}

}
