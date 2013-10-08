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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.javatuples.Quartet;
import de.tu_berlin.dima.oligos.db.constraints.ForeignKey;
import de.tu_berlin.dima.oligos.db.handler.jdbc.ForeignKeysHandler;
import de.tu_berlin.dima.oligos.db.handler.meta.ColumnRefsHandler;
import de.tu_berlin.dima.oligos.db.handler.meta.SchemaRefsHandler;
import de.tu_berlin.dima.oligos.db.handler.meta.TableRefsHandler;
import de.tu_berlin.dima.oligos.db.reference.ColumnRef;
import de.tu_berlin.dima.oligos.db.reference.EmptyRef;
import de.tu_berlin.dima.oligos.db.reference.SchemaRef;
import de.tu_berlin.dima.oligos.db.reference.TableRef;
import de.tu_berlin.dima.oligos.type.Types;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

/**
 * Wraps a database connection and provides convenience methods for meta data
 * access and query execution. Every interaction with the database should happen
 * through this class, rather than using the {@link Connection} directly.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 * @author Marie Hoffmann
 *
 */
public class JdbcConnector {
 
  private final Connection connection;
  private final DatabaseMetaData metaData;

  /**
   * Returns a new JdbcConnector wrapping the connection and caches the
   * database meta data from the connection.
   * @param connection
   * @throws SQLException if there occurs an error while retrieving the database
   *  meta data through the connection.
   * @since 0.3.1
   */
  public JdbcConnector(final Connection connection) throws SQLException {
    this.connection = connection;
    this.metaData = connection.getMetaData();
  }

  /******************************************************************************
   * RELATIONS AND ATTRIBUTES
   *****************************************************************************/
  
  /**
   * Retrieves all schemas from the current database connection.
   * @return all schemas for the current connection
   * @throws SQLException if there occurs an error while retrieving the database 
   */
  public Collection<SchemaRef> getSchemas() throws SQLException {
    AbstractListHandler<SchemaRef> handler = new SchemaRefsHandler();
    return handler.handle(metaData.getSchemas());
  }

  /**
   * @deprecated As of 0.3.1, replaced by {@link #getTables()} and
   * {@link #getTables(SchemaRef)}.
   * @param schema
   * @return
   * @throws SQLException
   */
  @Deprecated
  public Collection<String> getTables(final String schema) throws SQLException {
    ResultSet result = metaData.getTables(null, schema, null, null);
    List<String> tables = Lists.newArrayList();
    while (result.next()) {
      String table = result.getString("TABLE_NAME");
      tables.add(table);
    }
    DbUtils.close(result);
    return tables;
  }

  /**
   * Retrieves all tables from the current database connection.
   * @return all tables from the current connection
   * @throws SQLException if a database access error occurs
   * @since 0.3.1
   */
  public Collection<TableRef> getTables() throws SQLException {
    // I know ugly as f**k but that's the how jdbc rolls
    // essentially uses an EmptyRef object that always returns null
    return getTables(new EmptyRef());
  }

  /**
   * Retrieves all tables of the schema from the current connection.
   * @param schema 
   *  The schema reference of the tables
   * @return 
   *  all tables of the schema
   * @throws SQLException if a database access error occurs
   * @since 0.3.1
   */
  public Collection<TableRef> getTables(final SchemaRef schema) throws SQLException {
    String catalog = null;
    String schemaPattern = schema.getSchemaName();
    String tablePattern = null;
    String[] types = null;
    AbstractListHandler<TableRef> handler = new TableRefsHandler();
    ResultSet rs = metaData.getTables(catalog, schemaPattern, tablePattern, types);
    return handler.handle(rs);
  }

  /**
   * Retrieves all columns from the current database connection.
   * @return all columns of all schemas and tables
   * @throws SQLException if a database access error occurs
   */
  public Collection<ColumnRef> getColumns() throws SQLException {
    return getColumns(new EmptyRef());
  }

  /**
   * Retrieves all columns of the schema from the current database connection.
   * @param schema The schema reference to search the columns for
   * @return all columns of the schema
   * @throws SQLException if a database access error occurs
   */
  public Collection<ColumnRef> getColumns(final SchemaRef schema)
      throws SQLException {
    ResultSetHandler<List<ColumnRef>> handler = new ColumnRefsHandler();
    String schemaNamePattern = schema.getSchemaName();
    ResultSet rs =
        metaData.getColumns(null, schemaNamePattern, null, null);
    return handler.handle(rs);
  }

  /**
   * @deprecated As of version 0.3.1, replaced by {@link #getColumns(TableRef)}
   * @param schema
   * @param table
   * @return
   * @throws SQLException
   */
  @Deprecated
  public Collection<String> getColumns(final String schema, final String table) throws SQLException {
    ResultSet result = metaData.getColumns(null, schema, table, null);
    List<String> columns = Lists.newArrayList();
    while (result.next()) {
      String column = result.getString("COLUMN_NAME");
      columns.add(column);
    }
    DbUtils.close(result);
    return columns;
  }

  /**
   * Retrieves all columns of the table from the current database connection.
   * @param table The table reference to search the columns for
   * @return all columns of the table
   * @throws SQLException if a database access error occurs
   */
  public Collection<ColumnRef> getColumns(final TableRef table)
      throws SQLException {
    String catalog = null;
    String schemaPattern = null;
    String columnNamePattern = null;
    AbstractListHandler<ColumnRef> handler = new ColumnRefsHandler();
    ResultSet rs = metaData.getColumns(
        catalog, schemaPattern, table.getTableName(), columnNamePattern);
    return handler.handle(rs);
  }

  /******************************************************************************
   * REFERENCES
   *****************************************************************************/

  /**
   * @deprecated As of 0.3.1, replaced by
   *             {@link #getForeignKeys(SchemaRef))},
   *             {@link #getCrossReferences(SchemaRef, SchemaRef)},
   *             {@link #getImportedKeys(SchemaRef))}, or
   *             {@link #getExportedKeys(SchemaRef))}
   * @param schema
   * @return
   * @throws SQLException
   */
  @Deprecated
  public Set<Quartet<String, String, String, String>> getReferences(final String schema) throws SQLException{
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
   * Retrieves all foreign keys from the current database connection.
   * @return all foreign keys
   * @throws SQLException if a database access error occurs
   * @since 0.3.1
   */
  public Set<ForeignKey> getForeignKeys() throws SQLException {
    return getCrossReferences();
  }

  /**
   * Retrieves all foreign keys that are either imported, exported, or contained 
   * by the schema. That is all foreign keys, where the schema is either the
   * parent schema, the child schema, or both.
   * @return 
   *  all foreign keys for the schema
   * @throws SQLException if a database access error occurs
   * @since 0.3.1
   * @see {@link ForeignKey}
   */
  public Set<ForeignKey> getForeignKeys(SchemaRef schema) throws SQLException {
    Set<ForeignKey> fKeys = Sets.newHashSet();
    fKeys.addAll(getImportedKeys(schema));
    fKeys.addAll(getExportedKeys(schema));
    fKeys.addAll(getCrossReferences(schema, schema));
    return fKeys;
  }

  /**
   * Retrieves all foreign keys that are either imported, exported or contained
   * by the given table. That is all foreign keys, where the given table is
   * either the parent table, the child table, or both.
   * @return
   *  all foreign keys for the table
   * @throws SQLException if a database access error occurs
   * @since 0.3.1
   * @see {@link ForeignKey}
   */
  public Set<ForeignKey> getForeignKeys(TableRef table) throws SQLException {
    Set<ForeignKey> fKeys = Sets.newHashSet();
    fKeys.addAll(getImportedKeys(table));
    fKeys.addAll(getExportedKeys(table));
    return fKeys;
  }

  /**
   * Retrieves all foreign key relationships no matter the schema or table they
   * are in.
   * @return
   * @throws SQLException if a database access error occurs
   * @since 0.3.1
   * @see {@link ForeignKey}, {@link #getForeignKeys()}
   */
  public Set<ForeignKey> getCrossReferences() throws SQLException {
    // TODO increase performance
    Set<ForeignKey> fKeys = Sets.newHashSet();
    TableRef[] tables = getTables().toArray(new TableRef[1]);
    int length = tables.length;
    for (int m = 0; m < length; m++) {
      for (int n = m; n < length; n++) {
        fKeys.addAll(getCrossReferences(tables[m], tables[n]));
      }
    }
    return fKeys;
  }

  /**
   * Retrieves all foreign keys / references between the first and the second
   * schema.
   * @param firstSchema The first schema
   * @param secondSchema The second schema
   * @return all foreign keys between the first and the second schema
   * @throws SQLException if a database access error occurs
   * @since 0.3.1
   * @see {@link ForeignKey}
   */
  public Set<ForeignKey> getCrossReferences(
      final SchemaRef firstSchema,
      final SchemaRef secondSchema) throws SQLException {
    Set<ForeignKey> fKeys = Sets.newHashSet();
    Collection<TableRef> firstTables = getTables(firstSchema);
    Collection<TableRef> secondTables = getTables(secondSchema);
    for (TableRef firstTab : firstTables) {
      for (TableRef secondTab : secondTables) {
        fKeys.addAll(getCrossReferences(firstTab, secondTab));
      }
    }
    return fKeys;
  }

  /**
   * Retrieves all foreign keys / references between the first and the second
   * table.
   * @param firstTable The first table
   * @param secondTable The second table
   * @return all foreign keys between the first and the second table
   * @throws SQLException if a database access error occurs
   * @since 0.3.1
   * @see {@link ForeignKey}
   */
  public Set<ForeignKey> getCrossReferences(
      final TableRef firstTable,
      final TableRef secondTable) throws SQLException {
    Set<ForeignKey> fKeys = Sets.newHashSet();
    ResultSetHandler<Set<ForeignKey>> handler = new ForeignKeysHandler();
    String firstS = firstTable.getSchemaName();
    String firstT = firstTable.getTableName();
    String secondS = secondTable.getSchemaName();
    String secondT = secondTable.getTableName();
    ResultSet rs1 =
        metaData.getCrossReference(null, firstS, firstT, null, secondS, secondT);
    ResultSet rs2 =
        metaData.getCrossReference(null, secondS, secondT, null, firstS, firstT);
    fKeys.addAll(handler.handle(rs1));
    fKeys.addAll(handler.handle(rs2));
    return fKeys;
  }

  /**
   * Retrieves all foreign keys imported by the schema, i.e. foreign keys, where
   * the schema is the referencing/child schema.
   * @param schema The child schema
   * @return all foreign keys imported by the schema
   * @throws SQLException
   * @since 0.3.1
   * @see {@link ForeignKey}
   */
  public Set<ForeignKey> getImportedKeys(SchemaRef schema) throws SQLException {
    Set<ForeignKey> fKeys = Sets.newHashSet();
    Collection<TableRef> tables = getTables(schema);
    for (TableRef table : tables) {
      Set<ForeignKey> candidates = getImportedKeys(table);
      for (ForeignKey candidate : candidates) {
        if (!candidate.getParent().equals(schema)) {
          fKeys.add(candidate);
        }
      }
    }
    return fKeys;
  }

  /**
   * Retrieves all foreign keys imported by the table, i.e. foreign keys, where
   * the schema is the referencing/child table.
   * @param table The child table
   * @return all foreign keys imported by the table
   * @throws SQLException
   * @since 0.3.1
   * @see {@link ForeignKey}
   */
  public Set<ForeignKey> getImportedKeys(TableRef table) throws SQLException {
    ResultSetHandler<Set<ForeignKey>> handler = new ForeignKeysHandler();
    ResultSet rs = metaData.getImportedKeys(
        null, table.getSchemaName(), table.getTableName());
    return handler.handle(rs);
  }

  /**
   * Retrieves all foreign keys exported by the schema, i.e. foreign keys, where
   * the schema is the referenced/parent schema.
   * @param table The parent schema
   * @return all foreign keys exported by the schema
   * @throws SQLException
   * @since 0.3.1
   * @see {@link ForeignKey}
   */
  public Set<ForeignKey> getExportedKeys(SchemaRef schema) throws SQLException {
    Set<ForeignKey> fKeys = Sets.newHashSet();
    Collection<TableRef> tables = getTables(schema);
    for (TableRef table : tables) {
      Set<ForeignKey> candidates = getExportedKeys(table);
      for (ForeignKey candidate : candidates) {
        if (!candidate.getParent().equals(schema)) {
          fKeys.add(candidate);
        }
      }
    }
    return fKeys;
  }

  /**
   * Retrieves all foreign keys exported by the table, i.e. foreign keys, where
   * the table is the referenced/parent table.
   * @param table The parent table
   * @return all foreign keys exported by the table
   * @throws SQLException
   * @since 0.3.1
   * @see {@link ForeignKey}
   */
  public Set<ForeignKey> getExportedKeys(TableRef table) throws SQLException {
    ResultSetHandler<Set<ForeignKey>> handler = new ForeignKeysHandler();
    ResultSet rs = metaData.getExportedKeys(
        null, table.getSchemaName(), table.getTableName());
    return handler.handle(rs);
  }

  /**
   * @deprecated As of version 0.3.1, replaced by {@link #hasSchema(SchemaRef)}.
   * @param schema
   * @return
   * @throws SQLException
   */
  @Deprecated
  public boolean checkSchema(final String schema) throws SQLException {
    ResultSet result = metaData.getSchemas(null, schema);
    return result.next();
  }

  /**
   * Checks whether the schema exists in the current database connection or not.
   * @param schema The schema to check for existence
   * @return <code>true</code> if schema exists, <code>false</code> otherwise
   * @throws SQLException if a database error occurs
   */
  public boolean hasSchema(final SchemaRef schema) throws SQLException {
    ResultSet result = metaData.getSchemas(null, schema.getSchemaName());
    return result.next();
  }

  /**
   * @deprecated As of version 0.3.1, replaced by {@link #hasTable(TableRef)}
   * @param schema
   * @param table
   * @return
   * @throws SQLException
   */
  @Deprecated
  public boolean checkTable(final String schema, final String table) throws SQLException {
    ResultSet result = metaData.getTables(null, schema, table, null);
    return result.next();
  }

  /**
   * Checks whether the table exists in the current database connection or not.
   * @param table The table to check for existence
   * @return <code>true</code> if table exists, <code>false</code> otherwise
   * @throws SQLException if a database error occurs
   */
  public boolean hasTable(final TableRef table) throws SQLException {
    ResultSet result = metaData.getTables(
        null, table.getSchemaName(), table.getTableName(), null);
    return result.next();
  }

  /**
   * @deprecated As of version 0.3.1, replaced by {@link hasColumn(ColumnRef)}
   * @param schema
   * @param table
   * @param column
   * @return
   * @throws SQLException
   */
  public boolean checkColumn(final String schema, final String table, final String column) 
      throws SQLException {
    ResultSet result = metaData.getColumns(null, schema, table, column);
    return result.next();
  }

  /**
   * Checks whether the column exists in the current database connection or not.
   * @param column The column to check for existence
   * @return <code>true</code> if column exists, <code>false</code> otherwise
   * @throws SQLException if a database error occurs
   */
  public boolean hasColumn(final ColumnRef column) throws SQLException {
    ResultSet result = metaData.getColumns(
        null,
        column.getSchemaName(),
        column.getTableName(),
        column.getColumnName());
    return result.next();
  }

  /******************************************************************************
   * QUERY EXECUTION
   *****************************************************************************/
  
  /**
   * @deprecated As of 0.3.1, public methods should not return <code>ResultSet</code>.
   * @param query
   * @param parameters
   * @return
   * @throws SQLException
   */
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
    return runner.query(connection, query, handler, parameters);
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
