package de.tu_berlin.dima.oligos.db;

import java.sql.SQLException;

import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;

public interface MetaConnector {

  public boolean hasColumn(final ColumnId columnId) throws SQLException;

  public boolean hasColumn(final String schema, final String table, final String column)
      throws SQLException;

  public boolean hasStatistics(final ColumnId columnId) throws SQLException;

  public boolean hasStatistics(final String schema, final String table, final String column)
      throws SQLException;

  public boolean isEnumerated(final ColumnId columnId) throws SQLException;

  public boolean isEnumerated(final String schema, final String table, final String column)
      throws SQLException;

  public TypeInfo getColumnType(final ColumnId columnId) throws SQLException;

  public TypeInfo getColumnType(final String schema, final String table, final String column)
      throws SQLException;

}
