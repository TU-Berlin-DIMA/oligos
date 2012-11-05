package de.tu_berlin.dima.oligos.db;

import java.sql.SQLException;

public interface TableConnector {
  
  public long getCardinality(final String schema, final String table) throws SQLException;

}
