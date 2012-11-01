package de.tu_berlin.dima.oligos.stat;

import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.collect.Sets;

public class Schema {

  private final Set<Table> tables;
  private final UndirectedGraph<String, DefaultEdge> dependencies;
  
  public Schema() {
    this.tables = Sets.newHashSet();
    this.dependencies = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
  }
}
