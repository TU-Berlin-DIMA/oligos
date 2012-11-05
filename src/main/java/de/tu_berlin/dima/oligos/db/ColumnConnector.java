package de.tu_berlin.dima.oligos.db;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;

public interface ColumnConnector<T> {

  public TypeInfo getType() throws SQLException;

  public boolean hasStatistics() throws SQLException;

  public boolean isEnumerated() throws SQLException;

  public boolean isNullable() throws SQLException;

  public long getNumNulls() throws SQLException;
  
  public long getCardinality() throws SQLException;

  public Set<Constraint> getConstraints() throws SQLException;

  public Map<T, Long> getMostFrequentValues() throws SQLException;

  public Map<T, Long> getHistogram() throws SQLException;
}