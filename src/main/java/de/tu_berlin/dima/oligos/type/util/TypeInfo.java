package de.tu_berlin.dima.oligos.type.util;

public class TypeInfo {
  
  private final String typeName;
  private final int length;
  private final int scale;
  private final Class<?> type;
  
  public TypeInfo(final String typeName, final int length, final int scale, final Class<?> type) {
    this.typeName = typeName;
    this.length = length;
    this.scale = scale;
    this.type = type;
  }
  
  public String getTypeName() {
    return typeName;
  }
  
  public int getLength() {
    return length;
  }
  
  public int getScale() {
    return scale;
  }

  public Class<?> getType() {
    return type;
  }
  
  @Override
  public String toString() {
    return typeName + " -> " + type.getSimpleName() + " (" + length + ", " + scale + ")";
  }
}
