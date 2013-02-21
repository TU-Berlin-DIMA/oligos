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
            }
          }
        }
      }
      // TODO log non existent schema?
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
