package de.tu_berlin.dima.oligos.type.util;

public class Reference {
  
  private final Type type;
  private final ColumnId parent;
  private final ColumnId child;

  public Reference(final ColumnId parent, final ColumnId child, final Type type) {
    this.parent = parent;
    this.child = child;
    this.type = type;
  }

  public ColumnId getParent() {
    return parent;
  }

  public ColumnId getChild() {
    return child;
  }

  public Type getType() {
    return type;
  }
  
  public static enum Type {
    Foreign_Key;
  };
}
