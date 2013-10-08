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

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import de.tu_berlin.dima.oligos.DenseSchema;
import de.tu_berlin.dima.oligos.SparseSchema;
import de.tu_berlin.dima.oligos.type.util.ColumnId;


public class DbUtils {

	private static final Logger LOGGER = Logger.getLogger(DbUtils.class);
  
	public static DenseSchema sparseToDenseSchema(SparseSchema sparseSchema,
      JdbcConnector connector) throws SQLException {
    DenseSchema denseSchema = new DenseSchema();
    for (String schema : sparseSchema.schemas()) {
      Collection<String> tables = sparseSchema.tablesIn(schema);
      if (tables.isEmpty()) {
        tables = connector.getTables(schema);
      }
      for (String table : tables) {
        Collection<String> columns = sparseSchema.columnsIn(schema, table);
        if (columns.isEmpty()) {
          columns = connector.getColumns(schema, table);
        }
        for (String column : columns) {
          denseSchema.addColumn(new ColumnId(schema, table, column));
        }
      }
    }
    return denseSchema;
  }

	/**
	 * Collect columns for given schema, (table(s) (and column(s)))
	 * 
	 * @param sparseSchema
	 * @param connector
	 * @param metaConnector
	 * @return	DenseSchema object - set of columns 
	 * @throws SQLException
	 */
  public static DenseSchema populateSchema(SparseSchema sparseSchema, JdbcConnector connector, MetaConnector metaConnector)
      throws SQLException {
    DenseSchema denseSchema = new DenseSchema();
    for (String schema : sparseSchema.schemas()) {
      if (connector.checkSchema(schema)) {
        // Get the tables from the schema
        Collection<String> tables = sparseSchema.tablesIn(schema);
        if (tables.isEmpty()) {
          tables = connector.getTables(schema);
        }
        for (String table : tables) {
          if (connector.checkTable(schema, table)) {
            // Get the columns from the schema
            Collection<String> columns = sparseSchema.columnsIn(schema, table);
            if (columns.isEmpty()) {
              columns = connector.getColumns(schema, table);
            }
            for (String column : columns) {
              if (connector.checkColumn(schema, table, column)) {
                if (metaConnector.hasStatistics(schema, table, column)) {
                  denseSchema.addColumn(new ColumnId(schema, table, column));
                } else {
                  Logger logger = Logger.getLogger(DbUtils.class);
                  logger.warn(
                      "No statistics available for " + schema + "." + table + "." + column);
                }
              }
              else{
              	LOGGER.error("column does not exist for given schema and table");
              	System.exit(-1);
                
              }
            }
          }
          else{
          	LOGGER.error("table does not exist for given schema");
          	System.exit(-1);
          }
        }
      }
      else{
      	LOGGER.error("schema does not exist");
      	System.exit(-1);
      }
    }
    return denseSchema;
  }

  public static Map<ColumnId, Boolean> validateSchema(DenseSchema schema,
      JdbcConnector connector) throws SQLException {
    Map<ColumnId, Boolean> columns = Maps.newLinkedHashMap();
    for (ColumnId column : schema) {
      boolean exist =
          connector.checkColumn(column.getSchema(), column.getTable(), column.getColumn());
      columns.put(column, exist);
    }
    return columns;
  }
}
