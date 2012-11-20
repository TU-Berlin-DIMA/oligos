package de.tu_berlin.dima.oligos.stat;

import java.util.Iterator;
import java.util.Set;

import org.javatuples.Quartet;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Reference;

public class Schema implements Iterable<Table> {

  private final String schema;
  private final Set<Table> tables;
  private final DirectedGraph<ColumnId, Reference> references;

  public Schema(final String schema) {
    this.schema = schema;
    this.tables = Sets.newHashSet();
    this.references = new DefaultDirectedGraph<ColumnId, Reference>(Reference.class);
  }
  
  public Schema(final String schema, final Set<Table> tables
      , final Set<Quartet<String, String, String, String>> ris) {
    this.schema = schema;
    this.tables = tables;
    this.references = new DefaultDirectedGraph<ColumnId, Reference>(Reference.class);
    for (Quartet<String, String, String, String> q : ris) {
      String parentTable = q.getValue0();
      String paretColumn = q.getValue1();
      ColumnId parent = new ColumnId(schema, parentTable, paretColumn);
      String childTable = q.getValue2();
      String childColumn = q.getValue3();
      ColumnId child = new ColumnId(schema, childTable, childColumn);
      if (containsTable(parentTable) && containsTable(childTable)) {
        references.addVertex(parent);
        references.addVertex(child);
        Reference ref = new Reference(parent, child, Reference.Type.Foreign_Key);
        references.addEdge(child, parent, ref);
      }
    }
  }

  public String getName() {
    return schema;
  }

  public boolean containsTable(final String table) {
    for (Table tab : tables) {
      if (tab.getTable().equalsIgnoreCase(table)) {
        return true;
      }
    }
    return false;
  }

  public boolean isReference(final ColumnId columnId) {
    if (references.containsVertex(columnId)) {
      Set<Reference> neighbors = references.outgoingEdgesOf(columnId);
      return !neighbors.isEmpty();
    } else {
      return false;
    }
  }

  public ColumnId getReferencedColumn(final ColumnId columnId) {
    Set<Reference> neighbors = references.outgoingEdgesOf(columnId);
    // neighbors should only contain one neighbour
    for (Reference ref : neighbors) {
      return references.getEdgeTarget(ref);
    }
    return null;
  }

  @Override
  public Iterator<Table> iterator() {
    return tables.iterator();
  }

  @Override
  public String toString() {
    return schema + " " + tables.toString() + "\n" + references.toString();
  }
}
