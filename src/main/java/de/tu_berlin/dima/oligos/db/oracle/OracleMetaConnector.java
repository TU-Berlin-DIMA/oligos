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
import java.sql.SQLException;
import java.util.Map;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.MetaConnector;
import de.tu_berlin.dima.oligos.exception.ColumnDoesNotExistException;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;

public class OracleMetaConnector implements MetaConnector {
	
	private final static String ENUMERATED_QUERY = 
			"SELECT num_most_frequent, atc.num_distinct " +
			"FROM (SELECT COUNT(*) as num_most_frequent " +
			"FROM ALL_TAB_HISTOGRAMS WHERE owner = ? " +
			"AND table_name = ? AND column_name = ?), "+
			"ALL_TAB_COL_STATISTICS atc WHERE atc.owner = ? "+
			"AND atc.table_name = ? AND atc.column_name = ?";
	
	private final static String DOMAIN_QUERY =  
			"SELECT num_distinct FROM ALL_TAB_COLUMNS "+
			"WHERE owner = ? AND table_name = ? AND column_name = ?";

	private final static String TYPE_QUERY = 
			"SELECT data_type, data_length, data_scale "+
			"FROM ALL_TAB_COLUMNS WHERE owner = ? " +
			"AND table_name = ? "+
			"AND column_name = ?";
		  
	private final JdbcConnector connector;

	public OracleMetaConnector(final JdbcConnector jdbcConnector) {
		this.connector = jdbcConnector;
	}
	  
	@Override
	public boolean hasColumn(final ColumnId columnId) throws SQLException {
	    String schema = columnId.getSchema();
	    String table = columnId.getTable();
	    String column = columnId.getColumn();
	    return hasColumn(schema, table, column);
	  }

	@Override
	public boolean hasColumn(final String schema, final String table, final String column) throws SQLException {
		return connector.checkColumn(schema, table, column);
	}

	@Override
	public boolean hasStatistics(ColumnId columnId) throws SQLException {
		String schema = columnId.getSchema();
	    String table = columnId.getTable();
	    String column = columnId.getColumn();
	    return hasStatistics(schema, table, column);
	}

	@Override
	public boolean hasStatistics(String schema, String table, String column) throws SQLException {
		//System.out.println("Domain query for <schema> = " + schema + ", <table> = " + table +", <column> = " + column);
		BigDecimal aux = connector.scalarQuery(DOMAIN_QUERY, "NUM_DISTINCT", schema, table, column);
		Long card = aux.longValueExact();
		if (card != null) {
			return (card != -1) ? true : false; 
		} else {
			throw new ColumnDoesNotExistException(schema, table, column);
		}
	}

	@Override
	public boolean isEnumerated(ColumnId columnId) throws SQLException {
		String schema = columnId.getSchema();
	    String table = columnId.getTable();
	    String column = columnId.getColumn();
	    return isEnumerated(schema, table, column);
	}

	@Override
	public boolean isEnumerated(String schema, String table, String column) throws SQLException {
		Map<String, Object> result = connector.mapQuery(
	            ENUMERATED_QUERY, schema, table, column, schema, table, column);
	    if (result != null) {
	    	BigDecimal aux = (BigDecimal) result.get("NUM_DISTINCT");
	    	long colCard = (Long) aux.longValueExact();
	    	aux = (BigDecimal) result.get("NUM_MOST_FREQUENT");
	        int numMostFreq = aux.intValueExact();
	        return colCard <= numMostFreq;
	    } else {
	    	throw new ColumnDoesNotExistException(schema, table, column);
	    }
	}

	@Override
	public TypeInfo getColumnType(ColumnId columnId) throws SQLException {
		String schema = columnId.getSchema();
	    String table = columnId.getTable();
	    String column = columnId.getColumn();
	    return getColumnType(schema, table, column);
	}

	@Override
	public TypeInfo getColumnType(String schema, String table, String column) throws SQLException {
		return connector.typeQuery(schema, table, column);
			//return connector.typeQuery(TYPE_QUERY, schema, table, column);
	
	}

}
