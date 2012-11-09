package de.tu_berlin.dima.oligos.db;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import de.tu_berlin.dima.oligos.type.util.Constraint;

public interface ColumnConnector<T> {

  public boolean hasStatistics() throws SQLException;

  public boolean isEnumerated() throws SQLException;

  public boolean isNullable() throws SQLException;

  public long getNumNulls() throws SQLException;
  
  public long getCardinality() throws SQLException;

  public Set<Constraint> getConstraints() throws SQLException;

  public T getMin() throws SQLException;

  public T getMax() throws SQLException;

  public Map<T, Long> getMostFrequentValues() throws SQLException;

  public Map<T, Long> getHistogram() throws SQLException;
}