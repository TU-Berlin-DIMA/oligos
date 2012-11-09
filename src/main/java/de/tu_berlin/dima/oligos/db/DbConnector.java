package de.tu_berlin.dima.oligos.db;

import java.util.Set;

import com.google.common.collect.Sets;

public class DbConnector {

  private final MetaConnector metaConnector;
  private final Set<TableConnector> tableConnectors;
  private final Set<ColumnConnector<?>> columnConnectors;

  public DbConnector(final MetaConnector metaConnector) {
    this.metaConnector = metaConnector;
    this.tableConnectors = Sets.newLinkedHashSet();
    this.columnConnectors = Sets.newLinkedHashSet();
  }

  public void addTableConnector(final TableConnector tableConnector) {
    this.tableConnectors.add(tableConnector);
  }

  public void addColumnConnector(final ColumnConnector<?> columnConnector) {
    this.columnConnectors.add(columnConnector);
  }

  
}
