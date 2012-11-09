package de.tu_berlin.dima.oligos.db;

import java.sql.SQLException;
import java.util.Set;

import org.javatuples.Quartet;

public interface SchemaConnector {

  public Set<Quartet<String, String, String, String>> getReferences(final String schema)
      throws SQLException;

}
