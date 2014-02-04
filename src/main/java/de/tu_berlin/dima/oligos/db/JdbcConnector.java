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
package de.tu_berlin.dima.oligos.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.javatuples.Quartet;

import de.tu_berlin.dima.oligos.Driver;
import de.tu_berlin.dima.oligos.db.constraints.ForeignKey;
import de.tu_berlin.dima.oligos.db.handler.jdbc.ForeignKeysHandler;
import de.tu_berlin.dima.oligos.type.Types;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class JdbcConnector {
 

  private final String hostname;
  private final int port;
  private final String database;
  public final Driver dbDriver;
  
  private Connection connection;
  private DatabaseMetaData metaData;
  private static final Logger LOGGER = Logger.getLogger(JdbcConnector.class);
  
  public JdbcConnector(final String hostname, final int port, final String database, final Driver dbDriver) {
    this.hostname = hostname;
    this.port = port;
    this.database = database;
    this.dbDriver = dbDriver;
    this.metaData = null;
  }

  public String getConnectionString() {
	  return String.format(this.dbDriver.JDBC_STRING, this.dbDriver.driverName, hostname, port, database);
  }

  public void connect(final String user, final String pass) throws SQLException {
  	LOGGER.debug("entering JdbcConnector:connect ..."); 
	  String cs = getConnectionString();
	  //DriverManager.setLogWriter( new PrintWriter(System.out) );
	  connection = DriverManager.getConnection(cs, user, pass);
	  metaData = connection.getMetaData();
	  LOGGER.debug("leaving JdbcConnector:connect"); 
	}

  public void close() throws SQLException {
	  connection.close();
  }

  public Collection<String> getSchemas() throws SQLException {
    List<String> schemas = Lists.newArrayList();
    ResultSet result = metaData.getSchemas();
    while (result.next()) {
      String schema = result.getString("TABLE_SCHEM");
      schemas.add(schema);
    }
    DbUtils.close(result);
    return schemas;
  }

  public Collection<String> getTables(final String schema) throws SQLException {
  	LOGGER.debug("entering JdbcConnector:getTables ..."); 
	  ResultSet result = metaData.getTables(null, schema, null, null);
    List<String> tables = Lists.newArrayList();
    while (result.next()) {
      String table = result.getString("TABLE_NAME");
      tables.add(table);
    }
    DbUtils.close(result);
    LOGGER.debug("leaving JdbcConnector:getTables ..."); 
	  return tables;
  }

  public Collection<String> getColumns(final String schema, final String table) throws SQLException {
  	LOGGER.debug("entering JdbcConnector:getColumns ..."); 
    ResultSet result = metaData.getColumns(null, schema, table, null);
    List<String> columns = Lists.newArrayList();
    while (result.next()) {
      String column = result.getString("COLUMN_NAME");
      columns.add(column);
    }
    DbUtils.close(result);
    LOGGER.debug("leaving JdbcConnector:getColumns"); 
    return columns;
  }

  /**
   * Use 
   * {@link JdbcConnector#getForeignKeys(String)},
   * {@link JdbcConnector#getImportedKeys(String, String)}, or
   * {@link JdbcConnector#getExportedKeys(String, String)} instead.
   * @param schema
   * @return
   * @throws SQLException
   */
  @Deprecated
  public Set<Quartet<String, String, String, String>> getReferences(final String schema) throws SQLException{
  	LOGGER.error("JdbcConnector:getReferences deprecated!"); 
    Set<Quartet<String, String, String, String>> references = Sets.newHashSet();
	  ResultSet result;
	  Collection<String> tables = this.getTables(schema);
	  for (String table: tables){
		  result = this.metaData.getExportedKeys(null, schema, table);
		  while (result.next()){
			  Quartet<String, String, String, String> ri = new Quartet<String, String, String, String>(	
				  	result.getString("PKTABLE_NAME"), 
			  		result.getString("PKCOLUMN_NAME"),
			  		result.getString("FKTABLE_NAME"), 
			  		result.getString("FKCOLUMN_NAME"));
			  references.add(ri);
		  }
	  }
	  return references;
  }

  /**
   * Retrieves all foreign keys from the given schema.
   * @param schema
   * @return A <code>Set</code> containing no, one, or more <code>ForeignKey</code>s.
   * @throws SQLException
   */
  public Set<ForeignKey> getForeignKeys(final String schema) throws SQLException {
    Preconditions.checkArgument(schema != null && !schema.isEmpty());
    Set<ForeignKey> fKeys = Sets.newHashSet();
    for (String table : getTables(schema)) {
      ResultSet result = metaData.getExportedKeys(null, schema, table);
      ResultSetHandler<Set<ForeignKey>> handler = new ForeignKeysHandler();
      Set<ForeignKey> fks = handler.handle(result);
      fKeys.addAll(fks);
    }
    return fKeys;
  }

  /**
   * Retrieves all keys imported by the given table. That is all tables that are
   * referenced by the given schema and table.
   * @param schema Name of the schema
   * @param table Name of the table
   * @return A <code>Set</code> of all <code>ForeignKey</code>s, where the given
   * table is the <em>child</em> table.
   * @throws SQLException
   */
  public Set<ForeignKey> getImportedKeys(final String schema, final String table)
    throws SQLException {
    Preconditions.checkArgument(schema != null && table != null);
    Preconditions.checkArgument(!schema.isEmpty() && !table.isEmpty());
    ResultSet result = metaData.getImportedKeys(null, schema, table);
    ResultSetHandler<Set<ForeignKey>> handler = new ForeignKeysHandler();
    return handler.handle(result);
  }

  /**
   * Retrieves all keys exported by the given table. That is all tables that are 
   * referencing the given table.
   * @param schema Name of the schema
   * @param table Name of the table
   * @return A <code>Set</code> of all <code>ForeignKey</code>s, where the given
   * table is the <em>parent</em> table.
   * @throws SQLException
   */
  public Set<ForeignKey> getExportedKeys(final String schema, final String table)
    throws SQLException {
    Preconditions.checkArgument(schema != null && table != null);
    Preconditions.checkArgument(!schema.isEmpty() && !table.isEmpty());
    ResultSet result = metaData.getExportedKeys(null, schema, table);
    ResultSetHandler<Set<ForeignKey>> handler = new ForeignKeysHandler();
    return handler.handle(result);
  }

  public boolean checkSchema(final String schema) throws SQLException {
    ResultSet result = metaData.getSchemas(null, schema);
    if (result.next()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean checkTable(final String schema, final String table) throws SQLException {
    ResultSet result = metaData.getTables(null, schema, table, null);
    if (result.next()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean checkColumn(final String schema, final String table, final String column) 
      throws SQLException {
    ResultSet result = metaData.getColumns(null, schema, table, column);
    if (result.next()) {
      return true;
    } else {
      return false;
    }
  }
  
  @Deprecated
  public ResultSet executeQuery(final String query, final Object... parameters) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(query);
    for (int i = 1; i <= parameters.length; i++) {
      stmt.setObject(i, parameters[i-1]);
    }
    ResultSet result = stmt.executeQuery();
    return result;
  }

  public <T> T scalarQuery(
      final String query,
      final String columnName,
      final Object... parameters) throws SQLException {
  	ResultSetHandler<T> handler = new ScalarHandler<T>(columnName);
    QueryRunner runner = new QueryRunner();
    T res = runner.query(connection, query, handler, parameters);
    return res;
  }
  
  /*
   *  Execute SQL commands with no return values, like registeration of 
   *  procedures.
   *  
   *  @param 	query				the query string
   *  @param 	parameters	optional parameters for the query string
   *  @return void
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void execSQLCommand(
	      final String query,
	      final Object... parameters) throws SQLException {
  	ResultSetHandler handler = new ScalarHandler();
  	QueryRunner runner = new QueryRunner();
  	runner.query(connection, query, handler, parameters);	
  }
  // TODO allow grouping transactions
  
  /* execute prepared statement */
  public void execPreparedStmt(final String stmt, final Object... parameters) throws SQLException{
  	PreparedStatement ps = null;
  	try{
  		ps = connection.prepareStatement(stmt);	
  		for (int i = 1; i <= parameters.length; i++) {
  	    ps.setString(i, (String) parameters[i-1]);
      }
  		ps.executeUpdate();
  		connection.commit();
  		
  	}
  	catch(SQLException e){
  		e.printStackTrace();
  	}
  	finally{
  		if (ps != null){
  			ps.close();
    	}
  	}
  }
  
  
  public Map<String, Object> mapQuery(
      final String query,
      final Object... parameters) throws SQLException {
    ResultSetHandler<Map<String, Object>> handler = new MapHandler();
    QueryRunner runner = new QueryRunner();
    return runner.query(connection, query, handler, parameters);
  }

  public <T> Map<T, Long> histogramQuery(
      final String query,
      final String keyColumnName,
      final String valueColumnName,
      final Parser<T> parser,
      final Object...parameters) throws SQLException {
  	ResultSetHandler<Map<T, Long>> handler = new HistogramHandler<T>(keyColumnName, valueColumnName, parser);
  	QueryRunner runner = new QueryRunner();
    Map<T, Long> ret = runner.query(connection, query, handler, parameters);
    return ret;
  }
  
  public TypeInfo typeQuery(
      final String schema,
      final String table,
      final String column) throws SQLException {
  	ResultSet result = metaData.getColumns(null, schema, table, column);
    if (result.next()){
    String typeName = (String) result.getString("TYPE_NAME");
    int length;
    int scale;
    int typeNo;
    length = result.getInt("COLUMN_SIZE");
    scale = result.getInt("DECIMAL_DIGITS");
    typeNo = result.getInt("DATA_TYPE");
    Class<?> type = Types.convert(typeNo, length);
    return new TypeInfo(typeName, length, scale, type);
    }
    else
    	return null;
  }
}
