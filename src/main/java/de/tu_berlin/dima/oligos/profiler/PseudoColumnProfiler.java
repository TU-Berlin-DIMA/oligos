package de.tu_berlin.dima.oligos.profiler;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.stat.Column;

public class PseudoColumnProfiler<T> implements Profiler<Column<T>> {
  
  final ColumnConnector<T> connector;
  
  public PseudoColumnProfiler(final ColumnConnector<T> connector) {
    this.connector = connector;
  }

  @Override
  public Column<T> profile() {
    return null;
  }

}
